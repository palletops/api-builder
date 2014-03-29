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
  and asserts all exception data matches the one of the schemas.  Only
  tests exceptions that pass test-filter-pred.  Checking must be
  enabled at compile time with *validate-errors*."
  [test-filter-pred]
  (fn validate-errors [m]
    (if (and *assert* *validate-errors*)
      (update-in m [:body-args]
                 (fn [ba]
                   (map
                    #(validate-errors-form
                      % test-filter-pred (-> m :meta :errors))
                    ba)))
      m)))

;;; # Validate arguments
(defn- arg-elements
  "Return the elements of an arg vector ignoring any '& symbol."
  [args]
  (filterv #(not= '& %) args))

(defn- has-varargs?
  "Predicate to test whether an arg vector contains varargs"
  [args]
  (= (last (butlast args)) '&))

(defn- one-args
  "Return a schema that matches args and types one to one."
  [types args]
  (mapv (fn [s arg]
          `(schema/one ~s ~(name arg)))
        types args))

(defn- arg-schema
  "Return an arg schema for args."
  [types args varargs?]
  (if varargs?
    (conj
     (one-args (butlast types) (butlast args))
     (last types))
    (one-args types args)))

(defn- arg-count-matches?
  [args sig]
  (= (inc (count args)) (count sig)))

(defn arg-and-ref
  "Ensure a symbolic argument, arg, can be referred to.
  Returns a tuple with a modifed argument and an argument reference."
  [arg]
  (let [arg (cond
             (map? arg) (if (not (:as arg))
                          (assoc arg :as (gensym "arg"))
                          arg)
             (vector? arg) (if (not (= :as (last (butlast arg))))
                             (vec (concat arg [:as (gensym "arg")]))
                             arg)
             :else arg)
        arg-ref (cond
                 (map? arg) (:as arg)
                 (vector? arg) (last arg)
                 :else arg)]
    [arg arg-ref]))

(defn- validate-arguments-form
  "Convert a body-arg to assert that arguments match schemas. "
  [[args & body] sigs]
  (let [matching-sigs (filter #(arg-count-matches? (arg-elements args) %) sigs)
        sig (first matching-sigs)]
    (cond
     (= 1 (count matching-sigs))
     (let [args-and-refs (map arg-and-ref args)
           refs (mapv second args-and-refs)
           varargs? (has-varargs? args)
           elements (arg-elements refs)]
       [(mapv first args-and-refs)
        `(do
           (schema/validate
            ~(arg-schema (rest sig) elements varargs?)
            ~(if (and varargs? (not (map? (last args))))
               `(concat
                 ~(vec (butlast elements))
                 ~(last elements))
               elements))
            (let [r# (do ~@body)]
             (schema/validate ~(first sig) r#)
             r#))])

     (empty? matching-sigs)
     (throw
      (ex-info (str "No matching arity in :sig for " args)
               {:sig sigs
                :args args}))

     (> (count matching-sigs) 1)
     (throw
      (ex-info (str "More than one matching arity in :sig for " args)
               {:sig sigs
                :args args})))))

(defn validate-arguments
  "A middleware that takes :sig metadata as a sequence of schema
  sequences (one for each arity) and asserts all arguments match one
  of the schema sequences.  The first element of each :sig element is
  the return type."
  []
  (fn validate-arguments [m]
    (if *assert*
      (update-in m [:body-args]
                 (fn [ba]
                   (map
                    #(validate-arguments-form % (-> m :meta :sig))
                    ba)))
      m)))
