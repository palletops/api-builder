(ns com.palletops.api-builder.api-defn
  "An API defn form that uses all stages"
  (:require
   [com.palletops.api-builder :refer [def-defn]]
   [com.palletops.api-builder.stage :refer :all]
   [com.palletops.api-builder.stage.log :refer :all]))


;;; # API defn
(def-defn defn-api
  [(validate-errors (constantly true))
   (validate-sig)
   (add-sig-doc)
   (log-scope)
   (log-entry)
   (log-exit)])
