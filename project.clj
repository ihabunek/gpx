(defproject gpx "0.1.0-SNAPSHOT"
  :description "Parser and visualizer for gpx tracks"
  :url "http://example.com/FIXME"
  :license {:name "MIT Lincense"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [
    [clj-time "0.11.0"]
    [compojure "1.4.0"]
    [korma "0.4.0"]
    [org.clojure/algo.generic "0.1.2"]
    [org.clojure/clojure  "1.8.0"]
    [org.clojure/data.json "0.2.6"]
    [org.clojure/data.zip "0.1.1"]
    [org.webjars/foundation "6.2.0"]
    [org.webjars/leaflet "0.7.7"]
    [postgresql "9.3-1102.jdbc41"]
    [ring-webjars "0.1.1"]
    [ring/ring-defaults "0.1.5"]
    [selmer "1.0.4"]
    [yogthos/config "0.8"]
  ]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler gpx.handler/app}
  :main gpx.core
  :target-path "target/%s"
  :profiles {
    :uberjar {:aot :all}
    :dev {
      :resource-paths ["config/dev"]
      :dependencies [[javax.servlet/servlet-api "2.5"]
                     [ring/ring-mock "0.3.0"]] }})
