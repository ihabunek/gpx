(ns gpx.core
  (:gen-class)
  (:require
        [clj-time.core :as t]
        [clojure.pprint :refer [pprint]]
        [gpx.geo :as geo]
        [gpx.parse :as parse]
        [gpx.util :refer [pairs zipxml]]
  ))

; --- Helpers ------------------------------------------------------------------

(defn average [col]
  (/ (reduce + col) (count col)))

(defn time-diff [p1 p2]
  (t/in-seconds
    (t/interval (:time p1) (:time p2))))

(defn elevation-diff [p1 p2]
  (- (:ele p2) (:ele p1)))

; --- Running ------------------------------------------------------------------

(defn distances [points]
  (map #(apply geo/dist-haversine %) (pairs points)))

(defn intervals [points]
  (map #(apply time-diff %) (pairs points)))

(defn elevations [points]
  (map #(apply elevation-diff %) (pairs points)))

(defn speeds [points]
  (map (partial * 3.6)
    (map / (distances points) (intervals points))))

; --- Totals -------------------------------------------------------------------

(defn distance [points]
  (reduce + (distances points)))

(defn duration [points]
  (t/interval (:time (first points)) (:time (last points))))

(defn elevation-gained [points]
  (reduce +
    (filter pos?
      (elevations points))))

(defn elevation-lost [points]
  (* -1 (reduce +
    (filter neg?
      (elevations points)))))

(defn bounds [points]
  (let [lats (map :lat points)
        lons (map :lon points)
        eles (map :ele points)]

    (list { :lat (apply min lats) :lon (apply min lons) :ele (apply min eles) }
          { :lat (apply max lats) :lon (apply max lons) :ele (apply max eles) })
  ))

(defn stats [track]
  (let [points (:points track)
        speeds (speeds points) ]
    { :total {
        :distance (distance points)
        :duration (t/in-seconds (duration points)) }
      :speed {
        :avg (average speeds)
        :max (apply max speeds) }
      :elevation {
        :gain (elevation-gained points)
        :loss (elevation-lost points)
        :diff (elevation-diff (first points) (last points)) }} ))

(defn parse-track [source-file]
  (let [track (parse/parse-gpx source-file)]
    { :points (:points track)
      :name (-> track :metadata :name)
      :meta (:metadata track)
      :stats (stats track) }))

; --- Main ---------------------------------------------------------------------

(defn -main
  [& args]
  (if (empty? args)
    (println "No input file given.")
    (pprint (parse/parse-gpx (first args))) ))
