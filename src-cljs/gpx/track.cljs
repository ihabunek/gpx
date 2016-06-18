(ns gpx.track
  (:require
    [ajax.core :refer [GET]]
    [gpx.map :refer [draw-map]]
    goog.string.format
))

(enable-console-print!)

(defn error-handler [err]
  (.error js/console
    "Failed loading track" (str err) ))

(defn coordinates [point]
  [(:lat point) (:lon point)])

(defn parse-datetime [string]
  (.parse js/Date string))

(defn segment-points
  "Extracts a list of coordinate pairs from a track segment"
  [segment]
  (map coordinates (:points segment)))

(defn segment-times
  "Extracts a list of timestamps from a track segment"
  [segment]
  (map parse-datetime
    (map :time
      (:points segment))))

(defn segment-elevations
  "Extracts a list of elevations from a track segment"
  [segment]
  (map :ele
    (:points segment)))

(defn track-points
  "Extracts a list of points from the track, joining segments together"
  [track]
  (let [segments (map segment-points (:segment track))]
    (if (= 1 (count segments))
      (first segments)
      (concat segments))))

(defn track-times [track]
  (let [segments (map segment-times (:segment track))]
    (if (= 1 (count segments))
      (first segments)
      (concat segments))))

(defn track-elevations [track]
  (let [segments (map segment-elevations (:segment track))]
    (if (= 1 (count segments))
      (first segments)
      (concat segments))))

(defn elevation-chart-config [times elevations]
  {:chart {:renderTo "elevation-chart"
           :zoomType "x"}
   :title {:text "Elevation"}
   :xAxis {:type "datetime"}
   :yAxis {:title {:text "Elevation"}}
   :legend {:enabled false}
   :credits {:enabled false}
   :series [{:name "Elevation"
             :data (map vector times elevations) }]
   })

(defn draw-elevation-chart [times elevations]
  (js/Highcharts.Chart.
    (clj->js
      (elevation-chart-config times elevations))))

(defn display-track [track]
  (let [points (track-points track)
        times (track-times track)
        elevations (track-elevations track)
        waypoints (:waypoint track)]
    (draw-map "track-map" points waypoints)
    (draw-elevation-chart times elevations)
  ))

(defn load-track [slug]
  (let [url (goog.string.format "/%s/data" slug)]
      (GET url {
        :handler display-track
        :error-handler error-handler
        :response-format :json
        :keywords? true })
    ))


(load-track js/window.slug)
