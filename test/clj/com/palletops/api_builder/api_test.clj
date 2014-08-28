(ns com.palletops.api-builder.api-test
  (:require
   [clojure.test :refer :all]
   [com.palletops.api-builder.api :refer :all]
   [schema.core :as schema]))

(defn-api f
  {:sig [[schema/Keyword :- schema/Keyword]]}
  [k] k)

(deftest f-test
  (is (= :a (f :a)))
  (is (thrown? Exception (f "a"))))
