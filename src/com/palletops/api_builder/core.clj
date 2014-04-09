(ns com.palletops.api-builder.core
  "Schema and functions for api-builder used across stages and top level."
  (:require
   [schema.core :as schema]))

(def ArityMap
  {:args [schema/Any]
   (schema/optional-key :conditions)
   {(schema/optional-key :pre) [schema/Any]
    (schema/optional-key :post) [schema/Any]}
   :body [schema/Any]})

(def DefnMap
  {:name clojure.lang.Symbol
   :arities [ArityMap]
   :meta (schema/maybe {schema/Keyword schema/Any})})

(def FnMap
  (assoc DefnMap :name (schema/maybe clojure.lang.Symbol)))

(defn assert*
  "Evaluates expr and throws an exception if it does not evaluate to
  logical true.  The exception message is constructed using `format`
  and the supplied `message`, passing in the additional `args` with
  pr-str called on them.  Returns x if it is logically true."
  [x message & args]
  (or x
      (throw (new AssertionError (apply format message args)))))

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
