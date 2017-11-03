(ns craigslist-scraper.googlesheets
  (:require [clj-http.client :as client]
            [cheshire.core :as cheshire]
            [craigslist-scraper.config :as config]))

;; The redirect-uri will receive the oauth2 code
(def redirect-uri "http://localhost:3000/google-spreadsheets-set-code")
; (def redirect-uri "urn:ietf:wg:oauth:2.0:oob")

;; This is a global, not per-session or anything...
(defonce current-access-token (atom nil))

;; Send the user to this URL
(defn google-oauth2-auth-url []
  (str
    "https://accounts.google.com/o/oauth2/auth"
    "?"
    (client/generate-query-string
      {:client_id (:oauth2-client-id (config/load-googlesheets))
       :response_type "code"
       :redirect_uri redirect-uri
       :scope "https://www.googleapis.com/auth/spreadsheets"})))

(defn google-oauth2-token-request [code]
  {:url "https://accounts.google.com/o/oauth2/token"
   :method :post
   :as :json
   :throw-entire-message? true
   :form-params {:code code
                 :client_id (:oauth2-client-id (config/load-googlesheets))
                 :client_secret (:oauth2-client-secret (config/load-googlesheets))
                 :redirect_uri redirect-uri
                 :grant_type "authorization_code"}})

;; Note: returns 401 when access token expires.
(defn spreadsheet-request [spreadsheet-id row access-token]
  {:url (str "https://sheets.googleapis.com/v4/spreadsheets/" spreadsheet-id "/values/A1:append")
   :method :post
   :accept :json
   :content-type :json
   :query-params {:valueInputOption "USER_ENTERED"
                  :insertDataOption "INSERT_ROWS"}

   ;; Adds "Authorization: Bearer <token>" header
   :oauth-token access-token

   :body (cheshire/encode
          {:values [row]})
   :as :json
   ;; For debugging
   :throw-exceptions false})

(defn add-row-to-spreadsheet [row]
  (client/request
     (spreadsheet-request
       (:sheet-id (config/load-googlesheets))
       row
       @current-access-token)))

;; TODO - also set the expiry date so we know when to renew it.
(defn code-to-access-token [code]
  (-> (google-oauth2-token-request code)
      (client/request)
      :body
      :access_token))

(defn set-code [code]
  (reset! current-access-token (code-to-access-token code)))

;; The flow is:
;; (auth) -> code -> (token) -> access_token,expires_in,refresh_token,token_type
;;
;; Codes are one-use. The "code -> (token)" transition can only be done once per code.
