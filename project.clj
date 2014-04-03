(defproject com.palletops/api-builder "0.1.1-SNAPSHOT"
  :description "Write api functions with domain information."
  :url "http://github.com/palletops/api-builder"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.macro "0.1.2"]
                 [com.palletops/log-config "0.1.2"]
                 [prismatic/schema "0.2.1"]
                 [com.taoensso/timbre "3.1.6" :scope "provided"]])
