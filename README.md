# craigslist-scraper

A program I wrote to scrape Vancouver apartment/housing postings from Craigslist,
look up transit times from Google Maps, and insert the results into a Google Sheet.

In order to run this program, you'll need to create the following files:

* `/config.edn`
* `/src/craigslist_scraper/user_travel.clj`

The files above have examples that can serve as templates (e.g. `/config.example.edn`).

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

```
lein ring server
```

In order to insert data into the Google Sheet, you'll need to authorize this
application to write to it.
This is done by putting your Google account into an OAuth2 authorization flow.

Visit the link to start the OAuth2 flow: http://localhost:3000/google-spreadsheets-auth

You'll be redirected to Google, who will ask your Google account for permission
for a project (that you configured) to access Google Sheets.
