(defproject craigslist-scraper "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [clj-http "3.7.0"]
                 [cheshire "5.8.0"]
                 [clj-time "0.14.0"]
                 [hickory "0.7.1"]]
                 
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler craigslist-scraper.handler/app
         :nrepl {:start? true}}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]
                        [midje "1.8.3"]]
         :plugins [[lein-midje "3.2.1"]]}})
