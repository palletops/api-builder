# domain-fn

A Clojure library designed to let you write functions that express domain level concepts.

## Usage

The core of the library is a middleware for writing functions.  You
define a `defn` form using a list of middleware.

```clj
(def middleware [(validate-errors add-meta])

(def-defn mydefn middleware)
```

A middleware is a function that takes and returns a
[`DefnMap`](https://github.com/palletops/domain-fn/blob/master/src/com/palletops/domain_fn.clj#L16).


## Built in Middleware

The `add-meta` middleware can be used to add constant values to a
functions metadata.

The `validate-errors` middleware can be used to validate the `ex-data`
of exceptions thrown from the function against a sequence of possible
schemas specified on the `:errors` metadata key.  Validation is
controlled by the `*errors*` and the `*validate-errors*` vars at
compile time.

The `validate-arguments` middleware validates arguments and return
value against the `:sig` metadata key.  The value of the key is a
sequence of schema sequences.  The first value of each sequence is the
return type.  A vararg final arg is represented as a single schema
that should match all varargs.

## License

Copyright © 2014 Hugo Duncan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
