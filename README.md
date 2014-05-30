# api-builder

A Clojure library for writing APIs. API functions and vars are
augmented versions of standard functions and vars. This augmentation
happens at compile time and can bring features like validation of
parameters based on data schemas, logging of parameters, augmentation
of docstring based on parameter schemas, or even tagging of functions
for later grouping in documentation.

To use it, add `[com.palletops/api-builder "0.2.0"]` to your
`:dependencies`.

## Motivation

When writing libraries we usually want to provide an API for them --a
subset of all the functions in the library that are meant for users to
use-- and we like to treat these API functions differently from the
rest of (more internal) functions. For example, we want API functions
to:

  - check the format and type of parameters
  - add the function's signature into the docstring
  - add information about which errors the function can return
  - ... and even be able group API functions in different groups based
    on domain or other concepts.

This library allows us to do that by building our own `def`, `defn`
and `defmulti` macros for API functions with a custom set of stages
that each add new properties to the functions created.

## Usage

This library provides forms to create new versions of clojure's `Var`
defining forms.

The `def-defn` form creates new versions of clojure's `defn`
forms. These custom `defn` forms augment the created functions with
extra behavior. This new behavior is defined in a pipeline of _stage_
functions, or _stages_.

Similarly, `def-def` creates enhanced versions of clojure's `def`
forms, and `def-defmulti` enhanced versions of `defmulti` forms.

For example, the provided `defn-api` macro creates functions with with
error validation, signature validation, addition of signature in
docstring, and a few logging concerns. This macro is defined as:

```clj
(def-defn defn-api
  [(validate-errors (constantly true))
   (validate-sig)
   (add-sig-doc)
   (log-scope)
   (log-entry)
   (log-exit)])
```

Once this `api-defn` macro is defined, we can use it do define our API
functions, for example:

```clj
> (require '[com.palletops.api-builder.api :refer [defn-api]])
> (require '[schema.core :as s])

> (defn-api my-fun
   "My API fun"
   {:domain :main-api
    :sig [[s/Any :- s/Keyword]]
    :errors [{:type :example}]}
    [x]
    (if (string? x)
      (keyword x)
      (throw
        (ex-info "Can't create a keyword"
          {:type :example}))))
```

We can then see how the `add-sig-doc` stage added the signature to the docstring:

```clj
> (doc my-fun)
-------------------------
user/my-fun
([x])
  My API fun

## Function Signatures
  - Str -> Keyword
```

And also how the parameter checks are in place:

```clj
> (my-fun "my-key")
:my-key
> (my-fun 1)
ExceptionInfo Value does not match schema: [(named (not (instance? java.lang.String 1)) "x")]  schema.core/validate (core.clj:165)
```

## Built in Stages

Api-builder comes with some useful pre-defined stages:

### Additional metadata

The `add-meta` stage can be used to add constant values to a
functions metadata.

### Error validation

For development purposes. Helps you validate that your functions
declare all the error conditions they can throw. These error
conditions should be declared in the function's metadata under the
`:errors`, and each condition is represented by a different value of
the `ex-data` field in the `ex-info` exception throw by the function.

Whether this error validation takes place at runtime is controlled by
the value of `*validate-errors*` at compile time (and also by
clojure's assertion validation: `*assert*`). This var needs to be set
to `false` at compile time to disable validation at runtime.

### Argument Validation

The `validate-sig` stage validates the function's arguments and return
values against the `:sig` metadata key that contains a list of
_function schemas_, one for each of the function arities. Each
function schema is in itself a sequence of schema as defined by
[prismatic/schema](https://github.com/prismatic/schema). The first
value of each sequence is the return type, followed for a schema for
each argument in the function. A vararg final arg is represented as a
single schema that should match all varargs.

The `validate-optional-sig` stage validates the function's arguments
and return values against the `:sig` metadata as for `validate-sig`,
but only if the `:sig` metadata is specified.

### Logging

The `log-scope` stage allows setting of [log-config][log-config]
logging scopes for `:domain`, `:tags`, and `:context`.

The `log-entry` stage logs function entry at `:trace` using [Timbre][timbre].

The `log-exit` stage logs function exit at `:trace` using [Timbre][timbre].

## Defining Custom Stages

A stage is a function that takes and a
[`DefnMap`](https://github.com/palletops/api-builder/blob/master/src/com/palletops/api_builder.clj#L16),
which is a representation of a defn form. The stage returns a modified
version of the map.

## License

Copyright © 2014 Hugo Duncan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[log-config]: https://github.com/palletops/log-config "log-config"
[timbre]: https://github.com/ptaoussanis/timbre "Timbre"
