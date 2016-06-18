(defproject gpx "0.1.0-SNAPSHOT"
  :description "Parser and visualizer for gpx tracks"
  :url "http://example.com/FIXME"
  :license {:name "MIT Lincense"
            :url "https://opensource.org/licenses/MIT"}

  :min-lein-version "2.6.1"

  :dependencies [
    [org.clojure/clojure  "1.8.0"]
    [org.clojure/algo.generic "0.1.2"]
    [org.clojure/data.json "0.2.6"]
    [org.clojure/data.zip "0.1.1"]
    [org.clojure/tools.logging "0.3.1"]

    [clj-time "0.11.0"]
    [compojure "1.4.0"]
    [log4j "1.2.16"]
    [org.slf4j/slf4j-log4j12 "1.6.2"]
    [ring-webjars "0.1.1"]
    [ring/ring-defaults "0.1.5"]
    [selmer "1.0.4"]
    [yogthos/config "0.8"]

    ; Webjars
    [org.webjars/foundation "6.2.0"]
    [org.webjars/highcharts "4.2.5"]
    [org.webjars/leaflet "0.7.7"]

    ; Database
    [korma "0.5.0-SNAPSHOT"]
    [clj-postgresql "0.4.0"]
    [com.mchange/c3p0 "0.9.5.2"]
    [postgresql "9.3-1102.jdbc41"]

    ; Clojurescript
    [org.clojure/clojurescript "1.9.36"]
    [cljs-ajax "0.5.5"]
  ]

  :cljsbuild {
    :builds {
      :dev {
        :source-paths ["src-cljs"]
        :compiler {
          :main gpx.track
          :output-to "resources/public/js/dist/track.js"
          :output-dir "resources/public/js/dist/assets/"
          :asset-path "js/dist/assets"
          :optimizations :none
          :pretty-print true
          :source-map true }}
      }}

  :plugins [[lein-ring "0.9.7"]
            [lein-cljsbuild "1.1.3"]]

  :ring {:handler gpx.handler/app}

  :main gpx.core

  :target-path "target/%s"

  :profiles {
    :uberjar {:aot :all}
    :dev {
      :resource-paths ["config/dev"]
      :dependencies [[javax.servlet/servlet-api "2.5"]
                     [ring/ring-mock "0.3.0"]] }})
