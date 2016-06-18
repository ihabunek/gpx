(ns gpx.handler
  (:require
      [clj-time.format :as f]
      [clojure.data.json :as json]
      [clojure.java.io :as io]
      [clojure.pprint :refer [pprint]]
      [compojure.core :refer :all]
      [compojure.route :as route]
      [gpx.core :as core]
      [gpx.db :as db]
      [gpx.parse :as parse]
      [gpx.util :as util]
      [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
      [ring.middleware.webjars :refer [wrap-webjars]]
      [ring.util.response :refer [redirect]]
      [selmer.filters :as filters]
      [selmer.parser :refer [render-file]]
  ))

; Disable template cache for development
(selmer.parser/cache-off!)

; --- Filters ------------------------------------------------------------------

(filters/add-filter! :distance util/format-distance)
(filters/add-filter! :duration util/format-duration)
(filters/add-filter! :speed util/format-speed)

; --- Routes -------------------------------------------------------------------

(defn not-found []
  (route/not-found
    (render-file "templates/404.html" {} )))

(defn index []
  (render-file "templates/index.html" {
    :recent-tracks (db/fetch-recent-tracks) } ))

(defn prepare-track [track]
  (assoc track :stats (core/track-stats track)))

(defn value-writer
  "Convert joda datetime objects to strings when encoding JSON."
  [key value]
  (if (instance? org.joda.time.DateTime value)
    (f/unparse (f/formatters :date-time) value)
    value))

(defn json-encode-track [track]
  (json/write-str track :value-fn value-writer))

(defn track [slug]
  (let [track (db/fetch-track slug)]
    (if (nil? track)
      (not-found)
      (render-file "templates/track.html" track) )))

(defn track-data [slug]
  (let [track (db/fetch-track-with-data slug)]
    (if (nil? track)
      (not-found)
      (json-encode-track track) )))

(defn upload [params]
  (let [tempfile (-> params :route :tempfile)
        track (parse/parse-gpx-file tempfile)
        stats (core/track-stats track)
        segments (:segment track)
        waypoints (:waypoint track)

        created-track
          (db/create-track!
            (:name track)
            (:metadata track)
            stats)]

    (doseq [segment segments :let [points (:points segment)]]
      (db/create-segment!
        (:id created-track)
        (map :lat points)
        (map :lon points)
        (map :ele points)
        (map :time points)))

    (doseq [wpt waypoints]
      (db/create-waypoint!
        (:id created-track)
        (:name wpt)
        (:lat wpt)
        (:lon wpt)
        (:ele wpt)
        (:time wpt)))

    (redirect (str "/" (:slug created-track)) :see-other) ))

(defroutes app-routes
  (GET "/" [] (index))
  (GET "/:slug{[A-Za-z0-9]{6}}" [slug] (track slug))
  (GET "/:slug{[A-Za-z0-9]{6}}/data" [slug] (track-data slug))
  (POST "/upload" {params :params} (upload params))
  (not-found))

; --- Application --------------------------------------------------------------

; TODO: disabled anti-forgety for now because i couldn't get it to work
(def site-config
  (-> site-defaults
      (assoc-in [:security :anti-forgery] false)))

(def app
  (wrap-webjars
    (wrap-defaults app-routes site-config)))
