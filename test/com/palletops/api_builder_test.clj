(ns com.palletops.api-builder-test
  (:require
   [clojure.test :refer :all]
   [com.palletops.api-builder :as api-builder :refer :all]))

(deftest defn-map-test
  (is (= {:name 'f-name
          :arities '[{:args [a]
                      :body [a]}]
          :meta {::x 1}}
         (#'api-builder/defn-map 'f-name [{::x 1} ['a] 'a]))
      "single body")
  (is (= {:name 'f-name
          :arities '[{:args [a] :body [a]}
                     {:args [a b] :body [a b]}]
          :meta {::x 1}}
         (#'api-builder/defn-map 'f-name [{::x 1} '([a] a) '([a b] a b)]))
      "multi-arity"))

(deftest fn-map-test
  (is (= {:name nil
          :arities '[{:args [a]
                      :body [a]}]
          :meta {::x 1}}
         (#'api-builder/fn-map [{::x 1} ['a] 'a]))
      "no name, single body")
  (is (= {:name 'f-name
          :arities '[{:args [a]
                      :body [a]}]
          :meta {::x 1}}
         (#'api-builder/fn-map ['f-name {::x 1} ['a] 'a]))
      "single body")
  (is (= {:name 'f-name
          :arities '[{:args [a] :body [a]}
                     {:args [a b] :body [a b]}]
          :meta {::x 1}}
         (#'api-builder/fn-map ['f-name {::x 1} '([a] a) '([a b] a b)]))
      "multi-arity"))

(deftest defn-form-test
  (is (= '(clojure.core/defn f-name [a] a)
         (#'api-builder/defn-form
           {:name 'f-name
            :arities '[{:args [a]
                        :body [a]}]
            :meta {::x 1}}))
      "single body")
    (is (= '(clojure.core/defn f-name [a] {:pre [a]} a)
         (#'api-builder/defn-form
           {:name 'f-name
            :arities '[{:args [a]
                        :conditions {:pre [a]}
                        :body [a]}]
            :meta {::x 1}}))
      "single body with conditions")
    (is (= '(clojure.core/defn f-name ([a] a)([a b] a b))
         (#'api-builder/defn-form
           {:name 'f-name
            :arities '[{:args [a] :body [a]}
                     {:args [a b] :body [a b]}]
            :meta {::x 1}}))
        "multi-arity")
    (is (= '(clojure.core/defn f-name ([a] {:pre [a]} a)([a b] a b))
         (#'api-builder/defn-form
           {:name 'f-name
            :arities '[{:args [a] :conditions {:pre [a]} :body [a]}
                     {:args [a b] :body [a b]}]
            :meta {::x 1}}))
      "multi-arity with conditions"))

(deftest fn-form-test
  (is (= '(clojure.core/fn [a] a)
         (#'api-builder/fn-form
          {:name nil
           :arities '[{:args [a]
                       :body [a]}]
           :meta {::x 1}}))
      "no name, single body")
  (is (= '(clojure.core/fn f-name [a] a)
         (#'api-builder/fn-form
          {:name 'f-name
           :arities '[{:args [a]
                       :body [a]}]
           :meta {::x 1}}))
      "single body")
  (is (= '(clojure.core/fn f-name [a] {:pre [a]} a)
         (#'api-builder/fn-form
          {:name 'f-name
           :arities '[{:args [a]
                       :conditions {:pre [a]}
                       :body [a]}]
           :meta {::x 1}}))
      "single body with conditions")
  (is (= '(clojure.core/fn f-name ([a] a)([a b] a b))
         (#'api-builder/fn-form
          {:name 'f-name
           :arities '[{:args [a] :body [a]}
                      {:args [a b] :body [a b]}]
           :meta {::x 1}}))
      "multi-arity")
  (is (= '(clojure.core/fn f-name ([a] {:pre [a]} a)([a b] a b))
         (#'api-builder/fn-form
          {:name 'f-name
           :arities '[{:args [a] :conditions {:pre [a]} :body [a]}
                      {:args [a b] :body [a b]}]
           :meta {::x 1}}))
      "multi-arity with conditions"))
