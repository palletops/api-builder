(ns com.palletops.api-builder.stage.log
  "Stages to add logging"
  (:require
   [com.palletops.api-builder :refer [DefnMap]]
   [com.palletops.api-builder.stage :refer [arg-and-ref]]
   [com.palletops.log-config.timbre
    :refer [with-context with-domain with-tags]]
   [schema.core :as schema]
   [taoensso.timbre :refer [tracef]]))

;;; # Log scope
(defn add-log-scope
  [{:keys [args body] :as arity} domain tags context]
  (assoc arity
    :body (cond-> body
                  context (as-> x [`(with-context ~context ~@x)])
                  domain (as-> x [`(with-domain ~domain ~@x)])
                  tags (as-> x [`(with-tags ~tags ~@x)]))))

(defn log-scope
  "A stage that Uses the :domain, :tags and :context keys to configure
  the logging for the scope of the function.  :domain takes a
  keyword, :tags takes a set of keywords, :context a map of values."
  []
  (fn log-scope [m]
    {:pre [(schema/validate DefnMap m)]
     :post [(schema/validate DefnMap %)]}
    (update-in m [:arities]
               (fn [ba]
                 (map
                  #(add-log-scope
                    %
                    (-> m :meta :domain)
                    (-> m :meta :tags)
                    (-> m :meta :context))
                  ba)))))

;;; # Log function entry
(defn add-entry-logging
  [fname {:keys [args body] :as arity}]
  (let [args-and-refs (map arg-and-ref args)]
    (assoc arity
      :args (mapv first args-and-refs)
      :body (concat
             [`(tracef "%s entry" '~fname
                       {:args [~@(mapv second args-and-refs)]})]
             body))))

(defn log-entry
  "A stage that logs function entry.  The arguments are not shown in
  the log message, but are available on the message :args key."
  []
  (fn log-entry [m]
    {:pre [(schema/validate DefnMap m)]
     :post [(schema/validate DefnMap %)]}
    (update-in m [:arities]
               (fn [ba]
                 (map
                  #(add-entry-logging (:name m) %)
                  ba)))))

;;; # Log function exit
(defn add-exit-logging
  [fname {:keys [args body] :as arity}]
  (assoc arity
    :body [`(let [r# (do ~@body)]
             (tracef "%s exit" '~fname {:return-value r#})
             r#)]))

(defn log-exit
  "A stage that logs function exit.  The return value is not shown in
  the log message, but is available on the message :args key."
  []
  (fn log-exit [m]
    {:pre [(schema/validate DefnMap m)]
     :post [(schema/validate DefnMap %)]}
    (update-in m [:arities]
               (fn [ba]
                 (map
                  #(add-exit-logging (:name m) %)
                  ba)))))
