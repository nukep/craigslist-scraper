(ns craigslist-scraper.googlesheets-test
  (:require [craigslist-scraper.googlesheets :refer :all]
            [midje.sweet :refer :all]))


(facts "code-to-access-token works"
  (code-to-access-token ...code...) => ...access-token...
    (provided (clj-http.client/request (google-oauth2-token-request ...code...))
              => {:body {:access_token ...access-token...}}))

(facts "code-to-access-token throws exception if code is expired/already used"
  (code-to-access-token ...code...) => (throws Exception)
    (provided (clj-http.client/request (google-oauth2-token-request ...code...))
              =throws=> (ex-info "clj-http: status 400" {})))
