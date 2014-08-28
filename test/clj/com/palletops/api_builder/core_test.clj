(ns com.palletops.api-builder.core-test
  (:require
   [clojure.test :refer :all]
   [com.palletops.api-builder.core :refer :all]))

(deftest arg-and-ref-test
  (testing "fully specified map"
    (is (= '[{:keys [a b] :as c} c] (arg-and-ref '{:keys [a b] :as c}))))
  (testing "map with no as"
    (let [[arg ref] (arg-and-ref '{:keys [a b]})]
      (is ref)
      (is (= (:as arg) ref))))
  (testing "fully specified vector"
    (is (= '[[a b :as c] c] (arg-and-ref '[a b :as c]))))
  (testing "vector with no as"
    (let [[arg ref] (arg-and-ref '[a b])]
      (is ref)
      (is (= (last arg) ref))))
  (testing "plain symbol"
    (is (= '[a a] (arg-and-ref 'a)))))
