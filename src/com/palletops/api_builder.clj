(ns com.palletops.api-builder
  "Build augmented defn forms"
  (:require
   [clojure.string :as string]
   [com.palletops.api-builder.impl :refer :all]
   [clojure.tools.macro :refer [name-with-attributes]]))

(defmacro def-defn
  "Define a defn form, `name`, using the behaviour specified in the
  sequence `stages`."
  {:arglists '[[name doc? attr-map? stages]]}
  [name & args]
  (let [[name [stages]] (name-with-attributes name args)]
    `(defmacro ~name
       {:arglists '~defn-arglists}
       [n# & args#]
       (defn-impl ~stages n# args#))))

(defmacro def-fn
  "Define a fn form, `name`, using the behaviour specified in the
  sequence `stages`."
  {:arglists '[[name doc? attr-map? stages]]}
  [name & args]
  (let [[name [stages]] (name-with-attributes name args)]
    `(defmacro ~name
       {:arglists '~fn-arglists}
       [& args#]
       (fn-impl ~stages args#))))

(defmacro def-defmulti
  "Define a defmulti form, `name`, using the behaviour specified in
  the sequence `stages`. Also defines a method macro for defining
  associated methods.  The method macro is named the same as the
  defmulti form, with multi replaced by method, or if the defmulti
  form name doesn't contain \"multi\", then with \"-method\"
  appended."
  {:arglists '[[name doc? attr-map? stages]]} [name & args]
  (let [[name [stages]] (name-with-attributes name args)
        n (clojure.core/name name)
        method-name (symbol (if (.contains n "multi")
                              (string/replace n "multi" "method")
                              (str n "-method")))]
    `(do
       (defmacro ~name
         {:arglists '~defmulti-arglists}
         [n# & args#]
         (defmulti-impl ~stages n# args#))
       (defmacro ~method-name
         [n# dispatch-value# & args#]
         (defmethod-impl ~stages n# dispatch-value# args#)))))

(defmacro def-def
  "Define a def form, `name`, using the behaviour specified in the
  sequence `stages`."
  {:arglists '[[name doc? attr-map? stages]]}
  [name & args]
  (let [[name [stages]] (name-with-attributes name args)]
    `(defmacro ~name
       {:arglists '~def-arglists}
       [n# & args#]
       (def-impl ~stages n# args#))))
