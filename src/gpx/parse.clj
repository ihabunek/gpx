(ns gpx.parse
  (:require
    [clj-time.format :as f]
    [clj-time.core :as t]
    [clojure.data.zip :refer [rightmost?]]
    [clojure.data.zip.xml :refer [xml-> xml1-> attr text]]
    [clojure.zip :as zip]
    [gpx.util :refer [zipxml]]
  ))

(def multi-parser
  (f/formatter
    (t/default-time-zone)
    "yyyy-MM-dd'T'HH:mm:ssZ"
    "yyyy-MM-dd'T'HH:mm:ss.SSSZ"))

(defn parse-datetime [string]
  (if (empty? string) nil
    (try
      (f/parse multi-parser string)
      (catch Exception ex
        (throw (Exception. (str "Failed parsing datetime string: \"" string "\"") ex))))))

(def parse-double #(Double/parseDouble %))

(defn parse-wpt [wpt]
  { :lat (parse-double (attr wpt :lat))
    :lon (parse-double (attr wpt :lon))
    :ele (xml1-> wpt :ele text parse-double)
    :time (xml1-> wpt :time text parse-datetime)
    :name (xml1-> wpt :name text)
    :sym (xml1-> wpt :sym text)
    :type (xml1-> wpt :type text) })

(defn parse-trkpt [trkpt]
  { :lat (parse-double (attr trkpt :lat))
    :lon (parse-double (attr trkpt :lon))
    :ele (xml1-> trkpt :ele text parse-double)
    :time (xml1-> trkpt :time text parse-datetime) })

(defn parse-segment[trkseg]
  { :points (map parse-trkpt (xml-> trkseg :trkpt)) })

(defn parse-segments[gpx]
  (map parse-segment (xml-> gpx :trk :trkseg)))

(defn parse-metadata [metadata]
   { :name (xml1-> metadata :name text)
     :desc (xml1-> metadata :desc text)
     :time (xml1-> metadata :time text parse-datetime)
     :link (xml1-> metadata :link (attr :href)) })

(defn parse-names [gpx]
  (remove empty?
    [(xml1-> gpx :name text)
     (xml1-> gpx :trk :name text)
     (xml1-> gpx :metadata :name text)]))

(defn parse-name [gpx]
  (first (parse-names gpx)))

(defn parse-gpx [gpx]
  { :name (parse-name gpx)
    :segments (parse-segments gpx)
    :waypoints (map parse-wpt (xml-> gpx :wpt))
    :metadata (xml1-> gpx :metadata parse-metadata) })

(defn parse-gpx-file
  "Returns track information from a zipped gpx file"
  [file-path]
  (parse-gpx
    (zipxml file-path)))
