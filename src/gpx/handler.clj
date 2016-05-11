(ns gpx.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [selmer.parser :refer [render-file ]]
  ))

(defroutes app-routes
  (GET "/" [] (render-file "templates/index.html" {}))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
