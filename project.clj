(defproject gpx "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
    [org.clojure/clojure  "1.8.0"]
    [org.clojure/data.zip "0.1.1"]
    [org.clojure/algo.generic "0.1.2"]
    [clj-time "0.11.0"]
  ]
  :main gpx.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
