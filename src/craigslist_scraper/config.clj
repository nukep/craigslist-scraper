(ns craigslist-scraper.config
  (:require [clojure.edn :as edn]))

(defn load-config []
  (-> (slurp "config.edn" :encoding "utf-8")
      (edn/read-string)))

(defn load-googlesheets []
  (:googlesheets (load-config)))

(defn load-googlemaps []
  (:googlemaps (load-config)))

(defn user-agent []
  (:user-agent (load-config)))
