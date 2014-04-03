# domain-fn

A Clojure library designed to let you write API functions that express
domain level concepts.

## Usage

The library allows you to define your own `defn` forms that have extra
behaviour or data built into them.


```clj
(def stages [(validate-errors add-meta])

(def-defn mydefn stages)
```

The `def-defn` form is used to define new `defn` like forms.  You call
it with a sequence of stages, each of which adds something on top of
the base clojure defn.


## Built in Stages

The `add-meta` stage can be used to add constant values to a
functions metadata.

The `validate-errors` stage can be used to validate the `ex-data`
of exceptions thrown from the function against a sequence of possible
schemas specified on the `:errors` metadata key.  Validation is
controlled by the `*errors*` and the `*validate-errors*` vars at
compile time.

The `validate-sig` stage validates arguments and return
value against the `:sig` metadata key.  The value of the key is a
sequence of schema sequences.  The first value of each sequence is the
return type.  A vararg final arg is represented as a single schema
that should match all varargs.

The `log-scope` stage allows setting of [log-config][log-config]
logging scopes for `:domain`, `:tags`, and `:context`.

The `log-entry` stage logs function entry at `:trace` using [Timbre][timbre].

The `log-exit` stage logs function exit at `:trace` using [Timbre][timbre].

## Defining Stages

A stage is a function that takes and a
[`DefnMap`](https://github.com/palletops/api-builder/blob/master/src/com/palletops/api_builder.clj#L16),
which is a representation of a defn form.  The stage returns a
modified version of the map.

## License

Copyright © 2014 Hugo Duncan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[log-config]: https://github.com/palletops/log-config "log-config"
[timbre]: https://github.com/ptaoussanis/timbre "Timbre"
