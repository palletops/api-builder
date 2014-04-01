(ns com.palletops.api-builder-test
  (:require
   [clojure.test :refer :all]
   [com.palletops.api-builder :refer :all]))

(deftest defn-map-test
  (is (= {:name 'f-name
          :arities '[{:args [a]
                      :body [a]}]
          :meta {::x 1}}
         (defn-map 'f-name [{::x 1} ['a] 'a]))
      "single body")
  (is (= {:name 'f-name
          :arities '[{:args [a] :body [a]}
                     {:args [a b] :body [a b]}]
          :meta {::x 1}}
         (defn-map 'f-name [{::x 1} '([a] a) '([a b] a b)]))
      "multi-arity"))

(deftest defn-form-test
  (is (= '(clojure.core/defn f-name [a] a)
         (defn-form
           {:name 'f-name
            :arities '[{:args [a]
                        :body [a]}]
            :meta {::x 1}}))
      "single body")
    (is (= '(clojure.core/defn f-name [a] {:pre [a]} a)
         (defn-form
           {:name 'f-name
            :arities '[{:args [a]
                        :conditions {:pre [a]}
                        :body [a]}]
            :meta {::x 1}}))
      "single body with conditions")
    (is (= '(clojure.core/defn f-name ([a] a)([a b] a b))
         (defn-form
           {:name 'f-name
            :arities '[{:args [a] :body [a]}
                     {:args [a b] :body [a b]}]
            :meta {::x 1}}))
        "multi-arity")
    (is (= '(clojure.core/defn f-name ([a] {:pre [a]} a)([a b] a b))
         (defn-form
           {:name 'f-name
            :arities '[{:args [a] :conditions {:pre [a]} :body [a]}
                     {:args [a b] :body [a b]}]
            :meta {::x 1}}))
      "multi-arity with conditions"))
