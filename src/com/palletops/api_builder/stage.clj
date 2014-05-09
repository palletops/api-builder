(ns com.palletops.api-builder.stage
  "Stages for domain functions"
  (:require
   [clojure.string :refer [join]]
   [clojure.walk :refer [postwalk]]
   [com.palletops.api-builder.core
    :refer [arg-and-ref assert* ArityMap DefnMap]]
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
  [{:keys [args conditions body] :as arity} test-filter-pred error-schemas]
  {:pre [(schema/validate ArityMap arity)]
   :post [(schema/validate ArityMap %)]}
  (let [ex (gensym "e")]
    (assoc arity :body
           `[~@(if conditions [conditions])
             (try
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
                 (throw ~ex)))])))

(defn validate-errors
  "A stage that takes :errors metadata as a sequence of schemas,
  and asserts all exception data matches the one of the schemas.  Only
  tests exceptions that pass test-filter-pred.  Checking must be
  enabled at compile time with *validate-errors*."
  [test-filter-pred]
  (fn validate-errors [m]
    {:pre [(schema/validate DefnMap m)]
     :post [(schema/validate DefnMap %)]}
    (if (and *assert* *validate-errors*)
      (update-in m [:arities]
                 (fn [ba]
                   (map
                    #(validate-errors-form
                      % test-filter-pred (-> m :meta :errors))
                    ba)))
      m)))

;;; # Validate arguments
(def SigMap
  {:args [schema/Any]
   :return schema/Any})

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
  {:pre [(schema/validate SigMap sig)]}
  (= (count args) (count (:args sig))))

(defn- matching-sig
  [{:keys [args]} sigs]
  {:post [(schema/validate SigMap %)]}
  (let [matching-sigs (filter #(arg-count-matches? (arg-elements args) %) sigs)
        sig (first matching-sigs)]
    (cond
     (empty? matching-sigs)
     (throw
      (ex-info (str "No matching arity in :sig for " args)
               {:sig sigs
                :args args}))

     (> (count matching-sigs) 1)
     (throw
      (ex-info (str "More than one matching arity in :sig for " args)
               {:sig sigs
                :args args}))
     :else sig)))

(defn- validate-sig-arity
  "Convert a body-arg to assert that arguments match schemas. "
  [sigs {:keys [args conditions body] :as arity}]
  {:pre [(schema/validate ArityMap arity)]
   :post [(schema/validate ArityMap %)]}
  (let [sig (matching-sig arity sigs)
        varargs? (has-varargs? args)
        args-and-refs (map arg-and-ref args)
        refs (mapv second args-and-refs)
        elements (arg-elements refs)]
    (assoc arity
      :args (mapv first args-and-refs)
      :conditions (-> (or conditions {})
                      (update-in [:pre] (fnil conj [])
                                 `(schema/validate
                                   ~(arg-schema (:args sig) elements varargs?)
                                   ~(if (and varargs? (not (map? (last args))))
                                      `(concat
                                        ~(vec (butlast elements))
                                        ~(last elements))
                                      elements)))
                      (update-in [:post] (fnil conj [])
                                 `(or (schema/validate ~(:return sig) ~'%)
                                      ;; nil could be valid
                                      true))))))

(defn sig-map
  "Take a sig element and convert it into a map."
  [sig]
  {:pre [(vector? sig)]
   :post [(schema/validate SigMap %)]}
  (let [n (count sig)]
    (if (= :- (sig (- n 2)))
      {:args (subvec sig 0 (- n 2))
       :return (last sig)}
      {:args (subvec sig 0 (dec n))
       :return (last sig)})))

(defn validate-sig*
  [m]
  {:pre [(schema/validate DefnMap m)]
   :post [(schema/validate DefnMap %)]}
  (if *assert*
    (let [sigs (-> m :meta :sig)]
      (assert* (sequential? sigs)
               ":sig must be a sequence of vectors, but is %s" sigs)
      (assert*
       (every? vector? sigs)
       ":sig must be a sequence of vectors, but has non-vector elements %s"
       (remove vector? sigs))
      (update-in m [:arities]
                 (fn [arity]
                   (map #(validate-sig-arity (map sig-map sigs) %) arity))))
    m))

(defn validate-sig
  "A stage that takes :sig metadata as a sequence of schema
  sequences (one for each arity) and asserts all arguments match one
  of the schema sequences.  The last element of each :sig element is
  the return type."
  []
  validate-sig*)

(defn validate-optional-sig
  "A stage that optionally takes :sig metadata and validates it with
  validate-sig when it is present."
  []
  (fn validate-optionalsig [m]
    {:pre [(schema/validate DefnMap m)]
     :post [(schema/validate DefnMap %)]}
    (if (-> m :meta :sig)
      (validate-sig* m)
      m)))

;;; # Add sig to doc string
(defn remove-schema-ns
  [expr]
  (postwalk
   (fn [x]
     (if (symbol? x)
       (if-let [n (namespace x)]
         (let [tn ((symbol n) (ns-aliases *ns*))]
           (if (or (and tn (= (ns-name tn) 'schema.core))
                   (= (symbol n) 'schema.core))
             (symbol (name x))
             x))
         x)
       x))
   expr))

(defn format-sig
  [sig]
  (let [n (count sig)]
    (join " " (map remove-schema-ns (assoc-in sig [(- n 2)] "->")))))

(defn format-sigs
  [sigs]
  (str \newline \newline
       "## Function Signatures\n"
       (apply str
            (for [s (map format-sig sigs)]
              (str "\n  - " s)))))

(defn add-sig-doc
  "When given, add :sig metadata to the function's doc string."
  []
  (fn add-meta [defn-map]
    (if-let [sig (-> defn-map :meta :sig)]
      (update-in defn-map [:meta :doc] str (format-sigs sig))
      defn-map)))
