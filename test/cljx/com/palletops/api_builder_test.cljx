(ns com.palletops.api-builder-test
  #+clj (:require
         [clojure.test :refer :all]
         [com.palletops.api-builder.test-def-forms :refer [xf xd xm]])
  #+cljs (:require-macros
          [cemerick.cljs.test :refer [is deftest testing]]
          [com.palletops.api-builder.test-def-forms :refer [xf xd xm]])
  #+cljs (:require
          [cemerick.cljs.test :as t]))



(xf f [a] (keyword a))
(xd v :v)
(xm m (fn [x] x))

(defmethod m :default
  [x]
  (inc x))

(deftest test-defs
  (is (= :a (f "a")))
  (is (= :v v))
  (is (= 2 (m 1))))
