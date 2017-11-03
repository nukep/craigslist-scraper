(ns craigslist-scraper.scrape
  (:require [craigslist-scraper.craigslist :as craigslist]
            [craigslist-scraper.googlemaps :as googlemaps]
            [craigslist-scraper.googlesheets :as googlesheets]
            [craigslist-scraper.user-travel :as user-travel]
            [clj-time.local]))

(defn scrape-massage [craiglist-data])

(defn get-in-s [m ks]
  (if (keyword? ks)
    (get-in m [ks])
    (get-in m ks)))

(defn map-get-in [m ks]
  (map #(get-in-s m %) ks))

;; The keys for the values to extract from the merged map and insert as rows into the spreadsheet.
(defn spreadsheet-keys []
  (-> [:url :created :discovered :available :price :sqft :bedrooms :bathrooms :googlemaps :address]
      (into (map (fn [x] [x :summary-duration]) (user-travel/travel-keys)))
      (into (map (fn [x] [x :summary-vehicles]) (user-travel/travel-keys)))))

(defn scrape [url]
  (as-> url x
    (craigslist/download x)
    (assoc x :url url)
    (assoc x :discovered (str (clj-time.local/local-now)))
    (assoc x :googlemaps
             (format "https://www.google.ca/maps/search/%s,%s"
               (:lat x)
               (:lng x)))
    (merge x (googlemaps/summarize-travel (user-travel/travel-from-lat-lng (:lat x) (:lng x))))
    (map-get-in x (spreadsheet-keys))
    (googlesheets/add-row-to-spreadsheet x)))
