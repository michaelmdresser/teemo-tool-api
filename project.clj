(defproject teemo-tool-api "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "none"
            :url "none"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [ring "1.8.1"]
                 [ring/ring-json "0.5.0"]
                 [ring-cors/ring-cors "0.1.13"]
                 [compojure "1.6.1"]
                 [org.xerial/sqlite-jdbc "3.32.3"]]
  :main ^:skip-aot teemo-tool-api.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
