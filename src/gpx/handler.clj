(ns gpx.handler
  (:require
      [clojure.java.io :as io]
      [compojure.core :refer :all]
      [compojure.route :as route]
      [gpx.core :as core]
      [gpx.db :as db]
      [gpx.util :as util]
      [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
      [ring.util.response :refer [redirect]]
      [selmer.parser :refer [render-file]]
  ))

; Disable template cache for development
(selmer.parser/cache-off!)

; TODO: make sure file with generated id does not exist
; TODO: keep a hash to eliminate duplicates?
(defn upload [params]
  (let [tempfile (-> params :route :tempfile)
        track (core/parse-track tempfile)
        created (db/create-track! track)]
    (redirect (str "/" (:slug created)) :see-other) ))

(defn index []
  (render-file "templates/index.html" {} ))

(defn track [slug]
  (render-file "templates/track.html" (db/fetch-track slug) ))

(defroutes app-routes
  (GET "/" [] (index))
  (GET "/:slug" [slug] (track slug))
  (POST "/upload" {params :params} (upload params))
  (route/not-found "Not Found"))

; TODO: disabled anti-forgety for now because i couldn't get it to work
(def site-config
  (assoc-in site-defaults [:security :anti-forgery] false))

(def app
  (wrap-defaults app-routes site-config))
