(ns craigslist-scraper.googlemaps
  (:require [cheshire.core :as cheshire]
            [clj-http.client :as client]
            [craigslist-scraper.user-travel :as user-travel]))

(defn route-request [from to arrive-at]
  {:url "https://maps.googleapis.com/maps/api/directions/json"
   :method :get
   :query-params {:origin from
                  :destination to
                  :mode "transit"
                  ;; Return more than one route
                  ; :alternatives "true"
                  :arrival_time arrive-at}
                  ;; Arrival time is in epoch time (seconds)

   ;; Deserialize the response :body as JSON
   :as :json})

(defn seconds-to-rounded-minutes [s]
  (Math/round (float (/ s 60))))

;; How much time on the bus?
;; How much time on the train?
;; How many stops on the bus/train?
;; How much time walking?
;; How many transfers?
;; Total distance?
(defn summarize-steps [steps]
  {:per-vehicle
   (into {}
         (map (fn [[k v]] [k {:duration (reduce + (map :duration v))
                              :distance (reduce + (map :distance v))}]))
         (group-by :vehicle steps))
   :vehicles (->> steps
                  (filter #(= (:mode %) :transit))
                  (map :vehicle)
                  (vec))
   :total-duration (reduce + (map :duration steps))
   :total-distance (reduce + (map :distance steps))})

(defn simplify-step [step]
  (merge {:distance (get-in step [:distance :value])    ; metres
          :duration (get-in step [:duration :value])}   ; seconds
         (condp = (:travel_mode step)
           "WALKING"
           {:mode :walking
            :vehicle :walking}

           "TRANSIT"
           {:mode :transit
            :vehicle (-> (get-in step [:transit_details :line :vehicle :name])
                         clojure.string/lower-case
                         keyword)
            :short-name (get-in step [:transit_details :line :short_name])
            :num-stops (get-in step [:transit_details :num_stops])}

           {:mode :unknown})))


;; There should always be one leg if there are no waypoints.
;; legs = waypoints + 1
(defn parse-route [r]
  {:start-address  (-> r :legs (get 0) :start_address)
   ; :start-coords   (-> r :legs (get 0) :start_location)
   :end-address    (-> r :legs (get 0) :end_address)
   ; :end-coords     (-> r :legs (get 0) :end_location)
   :departure-time (-> r :legs (get 0) :departure_time :text)
   :arrival-time   (-> r :legs (get 0) :arrival_time :text)
   :duration       (-> r :legs (get 0) :duration :value)
   :steps          (map simplify-step (-> r :legs (get 0) :steps))})

(defn summarize-parsed-route [r]
  (let [ss (summarize-steps (:steps r))]
    {:summary-duration (seconds-to-rounded-minutes (:duration r))
     :summary-vehicles (->> (:vehicles ss)
                            (map (comp clojure.string/capitalize name))
                            (clojure.string/join " -> "))
     :time-waiting  (seconds-to-rounded-minutes (- (:duration r) (:total-duration ss)))
     :time-on-bus   (seconds-to-rounded-minutes (get-in ss [:per-vehicle :bus :duration] 0))
     :time-on-train (seconds-to-rounded-minutes (get-in ss [:per-vehicle :train :duration] 0))
     :time-walking  (seconds-to-rounded-minutes (get-in ss [:per-vehicle :walking :duration] 0))}))

(defn spit-debug [name contents]
  (spit (str "debug/" name ".edn") (str contents))
  contents)

(defn test-1 [from to arrive-at]
  (-> (->> (route-request from to arrive-at)
           (client/request)
           (spit-debug (str from "-" to "-" arrive-at))
           :body
           :routes
           (map parse-route)
           (map summarize-parsed-route))
      (nth 0 nil)))

(defn map-values [f coll]
  (reduce-kv #(assoc %1 %2 (f %3)) {} coll))

(defn summarize-travel [travels]
  (map-values #(test-1 (:from %) (:to %) (:arrive-at %)) travels))
