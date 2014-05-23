(ns com.palletops.api-builder.stage.log
  "Stages to add logging"
  (:require
   [com.palletops.api-builder.core
    :refer [arg-and-ref DefnMap DefFormMap DefMap DefmultiMap DefmethodMap]]
   [com.palletops.log-config.timbre
    :refer [with-context with-domain with-tags]]
   [schema.core :as schema]
   [taoensso.timbre :refer [tracef]]))

;;; # Log scope
(defn add-log-scope*
  [{:keys [args body] :as arity} domain tags context]
  (assoc arity
    :body (cond-> body
                  context (as-> x [`(with-context ~context ~@x)])
                  domain (as-> x [`(with-domain ~domain ~@x)])
                  tags (as-> x [`(with-tags ~tags ~@x)]))))

(defn add-log-scope
  [m meta]
  (update-in m [:arities]
             (fn [ba]
               (map
                #(add-log-scope*
                  %
                  (:domain meta)
                  (:tags meta)
                  (:context meta))
                ba))))

(defmulti log-scope*
  "A stage that Uses the :domain, :tags and :context keys to configure
  the logging for the scope of the function.  :domain takes a
  keyword, :tags takes a set of keywords, :context a map of values."
  (fn [m] (:type m)))

(defmethod log-scope* :defn
  [m]
  {:pre [(schema/validate DefnMap m)]
   :post [(schema/validate DefnMap %)]}
  (add-log-scope m (:meta m)))

(defmethod log-scope* :defmulti
  [m]
  {:pre [(schema/validate DefmultiMap m)]
   :post [(schema/validate DefmultiMap %)]}
  m)

(defmethod log-scope* :defmethod
  [m]
  {:pre [(schema/validate DefmethodMap m)]
   :post [(schema/validate DefmethodMap %)]}
  (add-log-scope m (-> (resolve (:name m)) meta)))

(defmethod log-scope* :def
  [m]
  {:pre [(schema/validate DefMap m)]
   :post [(schema/validate DefMap %)]}
  (throw (ex-info
          "Can not add a log-scope stage to a def form."
          {:m m})))

(defn log-scope
  "A stage that Uses the :domain, :tags and :context keys to configure
  the logging for the scope of the function.  :domain takes a
  keyword, :tags takes a set of keywords, :context a map of values."
  []
  log-scope*)

;;; # Log function entry
(defn add-entry-logging*
  [fname {:keys [args body] :as arity}]
  (let [args-and-refs (map arg-and-ref args)]
    (assoc arity
      :args (mapv first args-and-refs)
      :body (concat
             [`(tracef "%s entry" '~fname
                       {:args [~@(mapv second args-and-refs)]})]
             body))))

(defn add-entry-logging
  [m]
  (update-in m [:arities]
             (fn [ba]
               (map
                #(add-entry-logging* (:name m) %)
                ba))))

(defmulti log-entry* (fn [m] (:type m)))

(defmethod log-entry* :defn
  [m]
  {:pre [(schema/validate DefnMap m)]
   :post [(schema/validate DefnMap %)]}
  (add-entry-logging m))

(defmethod log-entry* :defmulti
  [m]
  {:pre [(schema/validate DefmultiMap m)]
   :post [(schema/validate DefmultiMap %)]}
  m)

(defmethod log-entry* :defmethod
  [m]
  {:pre [(schema/validate DefmethodMap m)]
   :post [(schema/validate DefmethodMap %)]}
  (add-entry-logging m))

(defn log-entry
  "A stage that logs function entry.  The arguments are not shown in
  the log message, but are available on the message :args key."
  []
  log-entry*)

;;; # Log function exit
(defn add-exit-logging*
  [fname {:keys [args body] :as arity}]
  (assoc arity
    :body [`(let [r# (do ~@body)]
             (tracef "%s exit" '~fname {:return-value r#})
             r#)]))
(defn add-exit-logging
  [m]
  (update-in m [:arities]
               (fn [ba]
                 (map
                  #(add-exit-logging* (:name m) %)
                  ba))))

(defmulti log-exit*
  "A stage that logs function exit.  The return value is not shown in
  the log message, but is available on the message :args key."
  (fn [m] (:type m)))

(defmethod log-exit* :defn
  [m]
  {:pre [(schema/validate DefnMap m)]
   :post [(schema/validate DefnMap %)]}
  (add-exit-logging m))

(defmethod log-exit* :defmulti
  [m]
  {:pre [(schema/validate DefmultiMap m)]
   :post [(schema/validate DefmultiMap %)]}
  m)

(defmethod log-exit* :defmethod
  [m]
  {:pre [(schema/validate DefmethodMap m)]
   :post [(schema/validate DefmethodMap %)]}
  (add-exit-logging m))

(defn log-exit
  "A stage that logs function exit.  The return value is not shown in
  the log message, but is available on the message :args key."
  []
  log-exit*)
