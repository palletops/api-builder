(defproject com.palletops/api-builder "0.3.1"
  :description "Write api functions with domain information."
  :url "http://github.com/palletops/api-builder"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/tools.macro "0.1.2"]
                 [prismatic/schema "0.4.2"]]
  :prep-tasks ["cljx" "javac" "compile"]
  :test-paths ["test/clj" "target/generated/test/clj"]
  :aliases {"auto-test" ["do" "clean," "cljx," "cljsbuild" "auto" "test"]
            "jar" ["do" "cljx," "jar"]
            "install" ["do" "cljx," "install"]
            "test" ["do" "cljx," "test"]}
  :cljsbuild {:builds []})
