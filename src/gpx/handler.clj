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

; --- Routes -------------------------------------------------------------------

(defn index []
  (render-file "templates/index.html" {} ))

(defn track [slug]
  (render-file "templates/track.html" (db/fetch-track slug) ))

(defn upload [params]
  (let [tempfile (-> params :route :tempfile)
        track (core/parse-track tempfile)
        created (db/create-track! track)]
    (redirect (str "/" (:slug created)) :see-other) ))

(defroutes app-routes
  (GET "/" [] (index))
  (GET "/:slug{[A-Za-z0-9]{6}}" [slug] (track slug))
  (POST "/upload" {params :params} (upload params))
  (route/not-found "Not Found"))

; --- Application --------------------------------------------------------------

; TODO: disabled anti-forgety for now because i couldn't get it to work
(def site-config
  (assoc-in site-defaults [:security :anti-forgery] false))

(def app
  (wrap-defaults app-routes site-config))
