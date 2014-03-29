(ns com.palletops.domain-fn.middleware-test
  (:require
   [clojure.test :refer :all]
   [com.palletops.domain-fn :as dfn]
   [com.palletops.domain-fn.middleware :refer :all]
   [schema.core :as schema]))

;;; # Test add-meta
(dfn/def-defn defn-add-meta
  [(add-meta {::x :x})])

(defn-add-meta f [])

(deftest add-meta-test
  (is (= :x (-> #'f meta ::x))))

;;; # Test validate-errors

;;; With assertions enabled
(alter-var-root #'*validate-errors* (constantly true))

(dfn/def-defn defn-validate-errors-always
  [(validate-errors '(constantly true))])

(defn-validate-errors-always v-e-a
  {:errors [{:type (schema/eq ::fred)}]}
  []
  (throw (ex-info "doesn't match" {:type ::smith})))

(dfn/def-defn defn-validate-errors-never
  [(validate-errors '(constantly false))])

(defn-validate-errors-never v-e-n
  {:errors [{:type (schema/eq ::fred)}]}
  []
  (throw (ex-info "some unkown error" {:type ::smith})))

;;; With assertions disabled
(alter-var-root #'*validate-errors* (constantly nil))

(dfn/def-defn defn-validate-errors-always-off
  [(validate-errors '(constantly true))])

(defn-validate-errors-always-off v-e-a-off
  {:errors [{:type (schema/eq ::fred)}]}
  []
  (throw (ex-info "some unkown error" {:type ::smith})))

(deftest validate-errors-test
  (is (thrown-with-msg?
       clojure.lang.ExceptionInfo #"Error thrown doesn't match :errors schemas"
       (v-e-a)))
  (is (thrown-with-msg?
       clojure.lang.ExceptionInfo #"some unkown error" (v-e-n)))
  (is (thrown-with-msg?
       clojure.lang.ExceptionInfo #"some unkown error" (v-e-a-off))))
