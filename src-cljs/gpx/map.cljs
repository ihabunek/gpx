(ns gpx.map)

(def tiles [{ :name "OpenStreetMap"
              :url "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              :options { :attribution "&copy; <a href=\"http://openstreetmap.org\">OpenStreetMap</a> contributors" }}
            { :name "OpenCycleMap"
              :url "http://{s}.tile.thunderforest.com/cycle/{z}/{x}/{y}.png"
              :options { :attribution "&copy; <a href=\"http://www.thunderforest.com/\">Thunderforest</a> &copy; <a href=\"http://www.openstreetmap.org/copyright\">OpenStreetMap</a>" }}
            { :name "Outdoors"
              :url "https://[abc].tile.thunderforest.com/outdoors/{z}/{x}/{y}.png"
              :options { :attribution "&copy; <a href=\"http://www.thunderforest.com/\">Thunderforest</a> &copy; <a href=\"http://www.openstreetmap.org/copyright\">OpenStreetMap</a>"
                         :maxZoom 22 }}
])

(defn create-layer [tile]
  (let [options (clj->js (:options tile))
        url (:url tile)]
    (-> js/L
      (.tileLayer url options))))

(def layers
  (map create-layer tiles))

(def layer-control
  (.control.layers js/L
    (clj->js
      (zipmap (map :name tiles) layers))))

(defn create-marker [wpt]
  (let [point #js [(:lat wpt) (:lon wpt)]]
    (-> js/L
      (.marker point)
      (.bindPopup (:name wpt)))))

(defn create-marker-layer-group [waypoints]
  (.layerGroup js/L
    (clj->js
      (map create-marker waypoints))))

(defn create-latlng [[lat lon]]
  (-> js/L (.latLng lat lon)))

(defn create-polyline [points]
  (.polyline js/L
    (clj->js (map create-latlng points))))

(defn draw-map [target-id points waypoints]
  (let [track-map (-> js/L (.map target-id))
        default-layer (first layers)
        path (create-polyline points)
        markers (create-marker-layer-group waypoints)]

    (.addTo default-layer track-map)
    (.addTo layer-control track-map)
    (.addTo markers track-map)
    (.addTo path track-map)

    (.fitBounds track-map
      (.getBounds path))
))
