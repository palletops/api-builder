{:dev {:plugins [[lein-pallet-release "0.1.3"]],
       :pallet-release
       {:url "https://pbors:${GH_TOKEN}@github.com/palletops/api-builder.git",
        :branch "master"}},
 :no-checkouts {:checkout-deps-shares ^{:replace true} []},
 :release {:set-version {:updates [{:path "README.md",
                                    :no-snapshot true}]}}}
