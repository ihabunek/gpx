(ns gpx.parse
  (:require
    [clj-time.format :as f]
    [clj-time.core :as t]
    [clojure.data.zip :refer [rightmost?]]
    [clojure.data.zip.xml :refer [xml-> xml1-> attr text]]
    [clojure.zip :as zip]
    [gpx.util :as util]
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

(defn trkpt-seq-inner [point]
  (lazy-seq
    (if (rightmost? point)
      (list point)
      (cons
        point
        (trkpt-seq-inner (zip/right point))))))

(defn trkpt-seq
  "Returns a lazy sequence of <trkpt> nodes from a zipped gpx file"
  [zgpx]
   (trkpt-seq-inner
     (xml1-> zgpx :trk :trkseg :trkpt))) ; the first <trkpt> node

(defn parse-wpt [wpt]
  { :lat (read-string (attr wpt :lat))
    :lon (read-string (attr wpt :lon))
    :ele (xml1-> wpt :ele text read-string)
    :time (xml1-> wpt :time text parse-datetime)
    :name (xml1-> wpt :name text)
    :sym (xml1-> wpt :sym text)
    :type (xml1-> wpt :type text) })

(defn parse-trkpt [trkpt]
  { :lat (read-string (attr trkpt :lat))
    :lon (read-string (attr trkpt :lon))
    :ele (xml1-> trkpt :ele text read-string)
    :time (xml1-> trkpt :time text parse-datetime) })

(defn parse-metadata [metadata]
   { :name (xml1-> metadata :name text)
     :desc (xml1-> metadata :desc text)
     :time (xml1-> metadata :time text parse-datetime)
     :link (xml1-> metadata :link (attr :href)) })

(defn parse-gpx [gpx]
  { :points (map parse-trkpt (trkpt-seq gpx))
    :waypoints (map parse-wpt (xml-> gpx :wpt))
    :metadata (some-> (:metadata gpx) parse-metadata) })

(defn parse-gpx-file
  "Returns track information from a zipped gpx file"
  [file-path]
  (parse-gpx-inner
    (util/zipxml file-path)))
