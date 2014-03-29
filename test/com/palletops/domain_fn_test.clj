(ns com.palletops.domain-fn-test
  (:require
   [clojure.test :refer :all]
   [com.palletops.domain-fn :refer :all]))

(deftest defn-map-test
  (is (= {:name 'f-name
          :body-args '[[[a] a]]
          :meta {::x 1}}
         (defn-map 'f-name [{::x 1} ['a] 'a]))
      "single body")
  (is (= {:name 'f-name
          :body-args '(([a] a)
                       ([a b] a b))
          :meta {::x 1}}
         (defn-map 'f-name [{::x 1} '([a] a) '([a b] a b)]))
      "multi-arity"))

(deftest defn-form-test
  (is (= '(clojure.core/defn f-name [a] a)
         (defn-form
           {:name 'f-name
            :body-args '(([a] a))
            :meta {::x 1}}))
      "single body")
  (is (= '(clojure.core/defn f-name ([a] a)([a b] a b))
         (defn-form
           {:name 'f-name
            :body-args '(([a] a)
                         ([a b] a b))
            :meta {::x 1}}))
      "multi-arity"))
