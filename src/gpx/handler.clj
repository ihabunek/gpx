(ns gpx.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.anti-forgery :refer :all]
            [selmer.parser :refer [render-file ]]
  ))

(def index
  (render-file "templates/index.html"
    { :csrf-field (anti-forgery-field) }))

(defroutes app-routes
  (GET "/" [] index)
  (POST "/upload" [foo] (println foo))
  (route/not-found "Not Found"))

(def handler
  (do
    (selmer.parser/cache-off!)
    (wrap-defaults app-routes site-defaults)))
