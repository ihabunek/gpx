(ns gpx.parse
  (:require
    [clj-time.format :as f]
    [clojure.data.zip :refer [rightmost?]]
    [clojure.data.zip.xml :refer [xml1-> attr text]]
    [clojure.zip :as zip]
  ))

(defn parse-datetime [string]
  (f/parse (f/formatters :date-time) string))

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

(defn points
  "Returns a lazy sequence of parsed points from a zipped gpx file"
  [zgpx]
  (map parse-trkpt
    (trkpt-seq zgpx)))
