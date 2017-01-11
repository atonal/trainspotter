(ns org.trainspotter.rata.api
  (:require [clj-http.lite.client :as client]
            [cheshire.core :as json]
            [org.trainspotter.utils :as utils]))

(def ^:const API_URL "https://rata.digitraffic.fi/api/v1/")

(def ^:const TRAIN_ID 8461)

(defmacro api-query [& more]
  `(let [response# (client/get (str API_URL ~@more) {:accept :json})
         status# (:status response#)]
     (assert (= status# 200) (str "Error:" status#))
     (json/parse-string (:body response#) true)))

(defn get-stations []
  "Returns a list of all available stations"
  (api-query "metadata/stations"))

(defn get-train [train-id ^org.joda.time.DateTime date-time]
  "Returns a train by its id on given date, or nil if no train is found"
  (let [date-str (utils/date-to-str date-time)]
    (first
      (api-query "live-trains/" train-id
                 "?departure_date=" date-str))))

(defn get-schedules [from to ^org.joda.time.DateTime date-time]
  "Returns a list of trains that go from 'from' to 'to' in the given date.
  'from' and 'to' are the short codes of the stations."
  (let [date-str (utils/date-to-str date-time)]
    (api-query "schedules?departure_station=" from
               "&arrival_station=" to
               "&departure_date=" date-str)))

(defn get-schedule-for-train [from to ^org.joda.time.DateTime date-time]
  "Returns a train that leaves from 'from' at the given time and goes by 'to',
  or nil if no train is found. 'from' and 'to' are the short codes of the stations."
  (let [date-time-str (utils/date-time-to-str date-time)]
    (first
      (api-query "schedules?departure_station=" from
                 "&arrival_station=" to
                 "&from=" date-time-str
                 "&limit=1"))))
