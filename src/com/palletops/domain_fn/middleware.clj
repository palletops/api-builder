(ns com.palletops.domain-fn.middleware
  "Middleware for domain functions"
  (:require
   [schema.core :as schema]))


;;; # Add Metadata
(defn add-meta
  "Add constant data to the function."
  [m]
  (fn add-meta [defn-map]
    (update-in defn-map [:meta] merge m)))


;;; # Validate Errors
(def ^:dynamic *validate-errors* nil)

(defn- validate-errors-form
  "Convert a body-arg to assert that exceptions match error-schemas."
  [[args & body] test-filter-pred error-schemas]
  (let [ex (gensym "e")]
    [args `(try
             ~@body
             (catch Exception ~ex
               (when (~test-filter-pred ~ex)
                 ~(if (seq error-schemas)
                    `(when (schema/check
                            (schema/either ~@error-schemas)
                            (ex-data ~ex))
                       (throw
                        (ex-info "Error thrown doesn't match :errors schemas"
                                 {:exception ~ex}
                                 ~ex)))
                    `(throw
                      (ex-info "Error thrown but no schemas to match against"
                               {:exception ~ex}
                               ~ex))))
               (throw ~ex)))]))

(defn validate-errors
  "A middleware that takes :errors metadata as a sequence of schemas,
  add asserts all exception data matches the one of the schemas.
  Only tests exceptions that pass test-filter-pred."
  [test-filter-pred]
  (fn error-types [m]
    (if (and *assert* *validate-errors*)
      (update-in m [:body-args]
                 (fn [ba]
                   (map
                    #(validate-errors-form
                      % test-filter-pred (-> m :meta :errors))
                    ba)))
      m)))
