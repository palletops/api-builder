{:provided {:dependencies [[org.clojure/clojure "1.6.0"]
                           [org.clojure/clojurescript "0.0-2277"]
                           [com.palletops/log-config "0.1.2"]
                           [com.taoensso/timbre "3.1.6" :scope "provided"]]}
 :dev {:dependencies [[com.keminglabs/cljx "0.4.0"]
                      [org.clojure/clojurescript "0.0-2277"]]
       :plugins [[lein-pallet-release "RELEASE"]
                 [com.cemerick/clojurescript.test "0.3.1"]
                 [com.cemerick/austin "0.1.5"]
                 [com.keminglabs/cljx "0.4.0"]
                 [lein-cljsbuild "1.0.3"]]
       :hooks [leiningen.cljsbuild]
       :cljx {:builds [{:source-paths ["test/cljx"]
                        :output-path "target/generated/test/clj"
                        :rules :clj}
                       {:source-paths ["test/cljx"]
                        :output-path "target/generated/test/cljs"
                        :rules :cljs}]}
       :cljsbuild {:test-commands
                   {"tests"      ["phantomjs" "runners/runner-none.js"
                                  "target/unit-test" "target/unit-test.js"]}
                   :builds
                   [{:id "test"
                     :source-paths ["src" "test/clj"
                                    "target/generated/test/cljs"]
                     :compiler {:output-to "target/unit-test.js"
                                :output-dir "target/unit-test"
                                :source-map "target/unit-test.js.map"
                                :optimizations :none
                                :pretty-print true}}]}}
 :jar {:cljsbuild {:builds []}
       :prep-tasks ["cljx" "javac" "compile"]}}
