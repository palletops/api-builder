(ns com.palletops.api-builder.stage.log-test
  (:require
   [clojure.test :refer :all]
   [com.palletops.api-builder.stage.log :refer :all]
   [com.palletops.api-builder :refer [def-defn]]
   [com.palletops.log-config.timbre :refer [context-msg domain-msg tags-msg]]
   [taoensso.timbre :refer [log example-config set-level!]]))

(def-defn def-log-scope-fn [(log-scope)])
(def-defn def-log-entry-fn [(log-entry)])
(def-defn def-log-exit-fn [(log-exit)])

(defn format-with-domain-context-tags
  [{:keys [domain context tags]} & _]
  (str (pr-str domain) (pr-str context) (pr-str tags)))

(def-log-scope-fn scope
  {:context {:a 1}
   :domain :d
   :tags #{:t1 :t2}}
  []
  (log (merge example-config
                 {:fmt-output-fn format-with-domain-context-tags
                  :middleware [context-msg domain-msg tags-msg]})
       :info "log %s" 1))

(def-log-entry-fn entry
  [] 1)

(def-log-exit-fn exit
  {}
  [] 1)

(deftest log-scope-test
  (is (= ":d{:a 1}#{:t1 :t2}\n" (with-out-str (scope)))))

(deftest log-entry-test
  (set-level! :trace)
  (is (re-find #"entry entry" (with-out-str (entry)))))

(deftest log-exit-test
  (set-level! :trace)
  (is (re-find #"exit exit" (with-out-str (exit))))
  (with-out-str
    (is (= 1 (exit)) "doesn't change return value")))
