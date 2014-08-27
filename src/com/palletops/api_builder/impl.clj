(ns com.palletops.api-builder.impl
  "Build augmented defn forms"
  (:require
   [clojure.string :as string]
   [clojure.tools.macro :refer [name-with-attributes]]
   [com.palletops.api-builder.core
    :refer [assert* ArityMap DefnMap DefmethodMap DefmultiMap DefMap FnMap]]
   [schema.core :as schema]))

;;; Capture the standard def forms' arglists
(def ^:internal defn-arglists (vec (:arglists (meta #'defn))))
(def ^:internal fn-arglists (vec (:arglists (meta #'fn))))
(def ^:internal defmulti-arglists (vec (:arglists (meta #'defmulti))))
(def ^:internal def-arglists '[[symbol doc-string? init?]])

(defn- arity-map
  "Return a map with a destructured defn arity (args, conditions and
  body)."
  [args]
  {:post [(schema/validate ArityMap %)]}
  (let [body (rest args)
        n (count body)
        condition-map (if (and (> (count body) 1)
                               (map? (first body)))
                        (first body))
        body (if condition-map
               (rest body)
               body)]
    (-> {:args (first args)
         :body body}
        (cond->
         condition-map (assoc :conditions condition-map)))))

(defn- defn-map
  "Return a map with destructured defn args."
  [n args]
  {:post [(schema/validate DefnMap %)]}
  (let [[n args] (name-with-attributes n args)
        m (meta n)]
    {:name n
     :arities (if (vector? (first args))
                [(arity-map args)]
                (map arity-map args))
     :meta m
     :type :defn}))

(defn- fn-map
  "Return a map with destructured fn args."
  [args]
  {:post [(schema/validate FnMap %)]}
  (let [[n args gensym?] (if (symbol? (first args))
                           [(first args) (rest args) nil]
                           [(gensym "fnname") args true])
        [n args] (name-with-attributes n args)
        m (meta n)]
    {:name (if-not gensym? n)
     :arities (if (vector? (first args))
                [(arity-map args)]
                (map arity-map args))
     :meta m
     :type :defn}))

(defn- defmulti-map
  "Return a map with destructured defmulti args."
  [n args]
  {:post [(schema/validate DefmultiMap %)]}
  (let [[doc args] (if (string? (first args))
                     [(first args) (rest args)]
                     [nil args])
        [attr-map args] (if (map? (first args))
                          [(first args) (rest args)]
                          [nil args])
        [dispatch-fn & options] args
        m (cond-> (merge (meta n) attr-map)
                  doc (assoc :doc doc))]
    {:name n
     :arities [(arity-map args)]
     :dispatch-fn dispatch-fn
     :meta m
     :options options
     :type :defmulti}))

(defn- defmethod-map
  "Return a map with destructured defmethod args."
  [n dispatch-val args]
  {:post [(schema/validate DefmethodMap %)]}
  {:name n
   :dispatch-value dispatch-val
   :arities [(arity-map args)]
   :type :defmethod})

(defn- def-map
  "Return a map with destructured def args."
  [n args]
  {:post [(schema/validate DefMap %)]}
  (let [[doc v] (if (= 1 (count args))
                  [nil (first args)]
                  args)
        m (cond-> (meta n)
                  doc (assoc :doc doc))]
    {:name n
     :value v
     :meta m
     :type :def}))

(defn- arity-form
  "Return a form for the arity map."
  [{:keys [args conditions body] :as arity}]
  `(~args ~@(if conditions [conditions]) ~@body))

(defn- defn-form
  "Return a defn form for a defn map"
  [{:keys [name arities meta] :as m}]
  {:pre [(schema/validate DefnMap m)]}
  `(defn ~(with-meta name meta)
     ~@(if (= 1 (count arities))
         (arity-form (first arities))
         (map arity-form arities))))

(defn- fn-form
  "Return a fn form for a defn map"
  [{:keys [name arities meta] :as m}]
  {:pre [(schema/validate FnMap m)]}
  `(fn ~@(if name [(with-meta name meta)])
     ~@(if (= 1 (count arities))
         (arity-form (first arities))
         (map arity-form arities))))

(defn- defmulti-form
  "Return a defmulti form for a defmulti map"
  [{:keys [name arities meta dispatch-fn] :as m}]
  {:pre [(schema/validate DefmultiMap m)]}
  (assert* (= 1 (count arities)) "Defmulti may only have a single arity")
  `(defmulti ~(with-meta name meta)
    ~dispatch-fn))

(defn- defmethod-form
  "Return a defmulti form for a defmulti map"
  [{:keys [name arities dispatch-value] :as m}]
  {:pre [(schema/validate DefmethodMap m)]}
  (assert* (= 1 (count arities)) "Defmethod may only have a single arity")
  `(defmethod ~name ~dispatch-value
     ~@(arity-form (first arities))))

(defn- def-form
  "Return a def form for a def map"
  [{:keys [name value meta] :as m}]
  {:pre [(schema/validate DefMap m)]}
  `(def ~(with-meta name meta) ~value))

(defn ^:internal defn-impl
  "Return a form to define a defn like macro, n, with arguments, args,
  using stages."
  [stages n args]
  (defn-form
    (reduce
     (fn [m f]
       {:pre [(schema/validate DefnMap m)]
        :post [(schema/validate DefnMap %)]}
       (f m))
     (defn-map n args)
     stages)))

(defn ^:internal fn-impl
  "Return a form to define a fn like macro, n, with arguments, args,
  using stages."
  [stages args]
  (fn-form
    (reduce
     (fn [m f]
       {:pre [(schema/validate FnMap m)]
        :post [(schema/validate FnMap %)]}
       (f m))
     (fn-map args)
     stages)))

(defn ^:internal defmulti-impl
  "Return a form to define a defmulti like macro, n, with arguments, args,
  using stages."
  [stages n args]
  (defmulti-form
    (reduce
     (fn [m f]
       {:pre [(schema/validate DefmultiMap m)]
        :post [(schema/validate DefmultiMap %)]}
       (f m))
     (defmulti-map n args)
     stages)))

(defn ^:internal defmethod-impl
  "Return a form to define a defmethod like macro, n, with arguments, args,
  using stages."
  [stages n dispatch-value args]
  (defmethod-form
    (reduce
     (fn [m f]
       {:pre [(schema/validate DefmethodMap m)]
        :post [(schema/validate DefmethodMap %)]}
       (f m))
     (defmethod-map n dispatch-value args)
     stages)))

(defn ^:internal def-impl
  "Return a form to define a def like macro, n, with arguments, args,
  using stages."
  [stages n args]
  (def-form
    (reduce
     (fn [m f]
       {:pre [(schema/validate DefMap m)]
        :post [(schema/validate DefMap %)]}
       (f m))
     (def-map n args)
     stages)))
