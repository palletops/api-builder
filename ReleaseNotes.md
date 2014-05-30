## 0.2.0

- Add def and defmulti form definers
  Adds `def-def` and `def-defmulti` for defining API vars and multimethods.

- Minor readme fix

- Name schemas for return values
  Closes #7

## 0.1.5

- Add stage to validate :sig only if present
  The `validate-optional-sig` stage will validate the `:sig` metadata only 
  if the `:sig` metadata is present.

  Also makes the `add-sig-doc` stage conditional on the presence of the
  `:sig` metadata.

## 0.1.4

- Fix namespace for defn-api

- Fix namespace in README example for defn-api

## 0.1.3

- Add def-fn for defining fn like forms
  Allow definition of fn like forms using stages.

## 0.1.2

- Allow doc string on def-defn
  Also allows adding metadata.

- Tweak signature addition to docstring.

## 0.1.1

- Add a defn-api that uses all the stages
  Closes #2

- Refactor namespaces
  Introduce com.palletops.api-builder.core which can be used from
  com.palletops.api-builder and com.palletops.api-builder.stage* without
  causing circular dependencies

- Add stage to add signature to :doc meta
  The add-sig-doc stage adds any :sig metadata to the function's doc string.

  Closes #1

- Add log-scope, log-entry and log-exit
  Closes #3

## 0.1.0

- Initial release
