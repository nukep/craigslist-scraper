(ns craigslist-scraper.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [craigslist-scraper.googlesheets :as googlesheets]))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/google-spreadsheets-set-code" [code]
    (println code)
    (googlesheets/set-code code)
    (str "The code is: " code))
  (GET "/google-spreadsheets-auth" []
    {:status 302
     :headers {"Location" (googlesheets/google-oauth2-auth-url)}})
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes api-defaults))
