(ns gpx.parse
  (:require
    [clj-time.format :as f]
    [clj-time.core :as t]
    [clojure.data.zip :refer [rightmost?]]
    [clojure.data.zip.xml :refer [xml1-> attr text]]
    [clojure.zip :as zip]
    [gpx.util :as util]
  ))

(def multi-parser
  (f/formatter
    (t/default-time-zone)
    "yyyy-MM-dd'T'HH:mm:ssZ"
    "yyyy-MM-dd'T'HH:mm:ss.SSSZ"))

(defn parse-datetime [string]
  (f/parse multi-parser string))

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

(defn parse-trkpt
  "Extracts the information from a gpx <trkpt> zipper"
  [zpoint]
    { :lat (read-string (attr zpoint :lat))
      :lon (read-string (attr zpoint :lon))
      :ele (read-string (xml1-> zpoint :ele text))
      :time (parse-datetime (xml1-> zpoint :time text)) })

(defn parse-metadata
  "Returns the gpx metadata
   See: http://www.topografix.com/GPX/1/1/#type_metadataType"
   [zgpx]
   { :name (xml1-> zgpx :metadata :name text)
     :desc (xml1-> zgpx :metadata :desc text)
     :time (parse-datetime (xml1-> zgpx :metadata :time text))
     :link (attr (xml1-> zgpx :metadata :link) :href) })

(defn parse-gpx-inner [zgpx]
  { :points (map parse-trkpt (trkpt-seq zgpx))
    :metadata (parse-metadata zgpx) })

(defn parse-gpx
  "Returns track information from a zipped gpx file"
  [file-path]
  (parse-gpx-inner
    (util/zipxml file-path)))
