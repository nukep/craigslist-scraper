(ns craigslist-scraper.googlemaps-test
  (:require [craigslist-scraper.googlemaps :refer :all]
            [midje.sweet :refer :all]
            [clojure.edn :as edn]))

(facts "seconds-to-rounded-minutes works"
  (seconds-to-rounded-minutes 0) => 0
  (seconds-to-rounded-minutes 29) => 0
  (seconds-to-rounded-minutes 30) => 1
  (seconds-to-rounded-minutes 60) => 1
  (seconds-to-rounded-minutes 300) => 5
  (seconds-to-rounded-minutes (- 3600 31)) => 59
  (seconds-to-rounded-minutes (- 3600 30)) => 60
  (seconds-to-rounded-minutes 3599) => 60
  (seconds-to-rounded-minutes 3600) => 60)

(def routes-1 (edn/read-string (slurp "test/googlemaps-routes-1.edn")))

(facts "")

(facts "parse-route works"
  (parse-route (get routes-1 0))
  => {:arrival-time "5:57pm"
      :departure-time "5:07pm"
      :duration 3009
      :end-address "Vancouver International Airport (YVR), 3211 Grant McConachie Way, Richmond, BC V7B 0A4, Canada"
      :start-address "Granville Island, Vancouver, BC, Canada"
      :steps '({:distance 214
                :duration 160
                :mode :walking
                :vehicle :walking}
               {:distance 1443
                :duration 480
                :mode :transit
                :num-stops 2
                :short-name "3"
                :vehicle :ferry}
               {:distance 638
                :duration 522
                :mode :walking
                :vehicle :walking}
               {:distance 12377
                :duration 1260
                :mode :transit
                :num-stops 9
                :short-name "Canada Line"
                :vehicle :train}
               {:distance 615
                :duration 449
                :mode :walking
                :vehicle :walking})}

  1 => 1)

(facts "summarize-steps works"
  (summarize-steps [])
  => {:vehicles [] :per-vehicle {} :total-distance 0 :total-duration 0}

  (summarize-steps [{:distance 214 :duration 160 :mode :transit :vehicle :bus}])
  => {:vehicles [:bus]
      :per-vehicle {:bus {:distance 214 :duration 160}}
      :total-distance 214
      :total-duration 160}

  (summarize-steps [{:distance 214
                     :duration 160
                     :mode :walking
                     :vehicle :walking}
                    {:distance 1443
                     :duration 480
                     :mode :transit
                     :num-stops 2
                     :short-name "3"
                     :vehicle :ferry}
                    {:distance 638
                     :duration 522
                     :mode :walking
                     :vehicle :walking}
                    {:distance 12377
                     :duration 1260
                     :mode :transit
                     :num-stops 9
                     :short-name "Canada Line"
                     :vehicle :train}
                    {:distance 615
                     :duration 449
                     :mode :walking
                     :vehicle :walking}])
  => {:vehicles [:ferry :train]
      :per-vehicle {:ferry {:distance 1443, :duration 480}
                    :train {:distance 12377, :duration 1260}
                    :walking {:distance 1467, :duration 1131}}
      :total-distance 15287
      :total-duration 2871})
