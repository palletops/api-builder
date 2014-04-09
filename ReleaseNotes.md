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
