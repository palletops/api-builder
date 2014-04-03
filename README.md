# api-builder

A Clojure library to for writing APIs. API functions are augmented
versions of standard functions. This augmentation happens at compile
time and can bring features like validation of parameters based on
data schemas, logging of parameters, augmentation of docstring based
on parameter schemas, or even tagging of functions for later grouping
in documentation.

## Usage

This library provides `def-defn` to create new versions of clojure's
`defn` forms. These custom `defn` forms augment the created functions with extra
behavior. This new behavior is defined in a pipeline of _stage_
functions, or _stages_.

In the following example, we are defining a `core-defn` macro to
define functions augmented with error validation and additional metadata:

```clj
(def stages [validate-errors add-meta])

(def-defn core-defn stages)
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

## Defining Custom Stages

A stage is a function that takes and a
[`DefnMap`](https://github.com/palletops/api-builder/blob/master/src/com/palletops/api_builder.clj#L16),
which is a representation of a defn form. The stage returns a modified
version of the map.

## License

Copyright © 2014 Hugo Duncan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
