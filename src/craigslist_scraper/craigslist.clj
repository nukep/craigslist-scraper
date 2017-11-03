(ns craigslist-scraper.craigslist
  (:require [clj-http.client :as client]
            [hickory.core :as hc]
            [hickory.select :as s]
            [craigslist-scraper.config :as config]))

(defn posting-request [url]
  {:url url
   :method :get
   :headers {"User-Agent" (config/user-agent)}})

(defn parse-square-feet [h]
  (as->
    (s/select (s/class "housing") h) x
    (-> x first :content first)
    (re-find #"([0-9]+)ft" x)
    (get x 1)))

(defn parse-created [h]
  (as->
    (s/select (s/class "postinginfo") h) x
    (filter #(= (-> % :content (get 0)) "Posted ") x)
    (nth x 0)
    (s/select (s/class "date") x)
    (-> x first :attrs :datetime)))

(defn flatten-text [h]
  (cond
    (string? h) h
    (vector? h) (vec (flatten (map flatten-text h)))
    (map? h) (recur (:content h))
    :else h))

(defn parse-bedrooms-bathrooms [string-vec]
  (let [s (clojure.string/join "" string-vec)]
    {:bedrooms (-> (re-find #"([0-9]+)BR" s) (get 1))
     :bathrooms (-> (re-find #"([0-9\.]+)Ba" s) (get 1))}))

(defn parse-hickory [h]
  (merge
    (-> (s/select (s/class "shared-line-bubble") h)
        first flatten-text parse-bedrooms-bathrooms)
    {:price (-> (s/select (s/class "price") h)
                first :content first)
     :lat   (-> (s/select (s/id "map") h)
                first :attrs :data-latitude)
     :lng   (-> (s/select (s/id "map") h)
                first :attrs :data-longitude)
     ;; Address isn't always there.
     :address (-> (s/select (s/and (s/tag :div) (s/class "mapaddress")) h)
                  first :content first)
     ;; Square feet is usually here if they specify.
     :sqft    (parse-square-feet h)
     :created (parse-created h)
     :available (-> (s/select (s/class "housing_movein_now") h)
                    first :content first)}))

(defn html-to-hickory [html]
  (hc/as-hickory (hc/parse html)))

(defn parse-posting-html [html]
  (parse-hickory (html-to-hickory html)))

(defn download-html [url]
  (-> (posting-request url)
      (client/request)
      :body))

(defn download [url]
  (-> (download-html url)
      (parse-posting-html)))
