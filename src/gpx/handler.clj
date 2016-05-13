(ns gpx.handler
  (:require
      [clojure.java.io :as io]
      [compojure.core :refer :all]
      [compojure.route :as route]
      [gpx.util :as util]
      [gpx.core :as core]
      [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
      [ring.util.response :refer [redirect]]
      [selmer.parser :refer [render-file]]
  ))

; Disable template cache for development
(selmer.parser/cache-off!)

; TODO: make sure file with generated id does not exist
; TODO: create an absolute target path
; TODO: keep a hash to eliminate duplicates?
(defn upload [params]
  (let [tempfile (-> params :route :tempfile)
        id (util/random-id)
        target (str "resources/uploads/" id ".gpx")]

    (io/copy tempfile (io/file target))
    (redirect (str "/track/" id) :see-other)))

(defn index []
  (render-file "templates/index.html" {} ))

(defn track [id]
  (let [source (io/resource (str "uploads/" id ".gpx"))
        track (core/parse-track source)]

    (render-file "templates/track.html" track )))

(defroutes app-routes
  (GET "/" [] (index))
  (GET "/:id" [id] (track id))
  (POST "/upload" {params :params} (upload params))
  (route/not-found "Not Found"))

; TODO: disabled anti-forgety for now because i couldn't get it to work
(def site-config
  (assoc-in site-defaults [:security :anti-forgery] false))

(def app
  (wrap-defaults app-routes site-config))
