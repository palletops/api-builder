(ns com.palletops.api-builder.api-logged
  "An API defn form that uses all stages"
  (:require
   [com.palletops.api-builder :refer [def-defn def-def def-defmulti]]
   [com.palletops.api-builder.stage :refer :all]
   [com.palletops.api-builder.stage.log :refer :all]))


;;; # API defn
(def-defn defn-api
  [(validate-errors (constantly true))
   (validate-sig)
   (add-sig-doc)
   (log-scope)
   (log-entry)
   (log-exit)
   (add-meta {:api true})])

(def-defmulti defmulti-api
  [(validate-errors (constantly true))
   (validate-sig)
   (add-sig-doc)
   (log-scope)
   (log-entry)
   (log-exit)
   (add-meta {:api true})])

(def-def def-api
  [(add-meta {:api true})])
