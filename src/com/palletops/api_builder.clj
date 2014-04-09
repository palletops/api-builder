(ns com.palletops.api-builder
  "Build augmented defn forms"
  (:require
   [clojure.tools.macro :refer [name-with-attributes]]
   [com.palletops.api-builder.core :refer [assert* ArityMap DefnMap FnMap]]
   [schema.core :as schema]))

(def ^:internal defn-arglists (vec (:arglists (meta #'defn))))
(def ^:internal fn-arglists (vec (:arglists (meta #'fn))))

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
     :meta m}))

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
     :meta m}))

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
