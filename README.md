# domain-fn

A Clojure library designed to let you write functions that express domain level concepts.

## Usage

The core of the library is a middleware for writing functions.  You
define a `defn` form using a list of middleware.

```clj
(def middleware [(validate-errors add-meta])

(def-defn mydefn middleware)
```

## Built in Middleware

The `add-meta` middleware can be used to add constant values to a
functions metadata.

The `validate-errors` middleware can be used to validate the `ex-data`
of exceptions thrown from the function against a sequence of possible
schemas specified on the `:errors` metadata key.  Validation is
controlled by the `*errors*` and the `*validate-errors*` vars at
compile time.

## License

Copyright © 2014 Hugo Duncan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
