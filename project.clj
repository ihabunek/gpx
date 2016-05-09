(defproject gpx "0.1.0-SNAPSHOT"
  :description "Parser and visualizer for gpx tracks"
  :url "http://example.com/FIXME"
  :license {:name "MIT Lincense"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [
    [org.clojure/clojure  "1.8.0"]
    [org.clojure/data.zip "0.1.1"]
    [org.clojure/algo.generic "0.1.2"]
    [clj-time "0.11.0"]
    [compojure "1.4.0"]
    [ring/ring-defaults "0.1.5"]
  ]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler gpx.handler/app}
  :main gpx.core
  :target-path "target/%s"
  :profiles {
    :uberjar {:aot :all}
    :dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                         [ring/ring-mock "0.3.0"]]}})
