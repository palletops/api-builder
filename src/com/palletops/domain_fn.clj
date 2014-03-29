(ns com.palletops.domain-fn
  "Build augmented defn forms"
  (:require
   [clojure.tools.macro :refer [name-with-attributes]]
   [schema.core :as schema]))

(def defn-arglists (vec (:arglists (meta #'defn))))

(def DefnMap {:name clojure.lang.Symbol
              :body-args [[(schema/one [schema/Any] "arg vector")
                           schema/Any]]
              :meta {schema/Keyword schema/Any}})

(defn defn-map
  "Return a map with destructured defn args."
  [n args]
  {:post [(schema/validate DefnMap %)]}
  (let [[n args] (name-with-attributes n args)
        m (meta n)]
    {:name n
     :body-args (if (vector? (first args))
                  [args]
                  args)
     :meta m}))

(defn defn-form
  "Return a defn form for a defn map"
  [{:keys [name body-args meta] :as m}]
  {:pre [(schema/validate DefnMap m)]}
  `(defn ~(with-meta name meta)
     ~@(if (= 1 (count body-args))
         (first body-args)
         body-args)))

(defn defn-impl
  [mw n args]
  (defn-form
    (reduce
     (fn [m f] (f m))
     (defn-map n args)
     mw)))

(defmacro def-defn
  "Define a defn form"
  [name middleware]
  `(defmacro ~name
     {:arglists '~defn-arglists}
     [n# & args#]
     (defn-impl ~middleware n# args#)))
