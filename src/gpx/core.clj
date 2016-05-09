(ns gpx.core
  (:gen-class)
  (:require
        [clj-time.core :as t]
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
  (map / (distances points) (intervals points)))

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

; --- Main ---------------------------------------------------------------------

(defn -main
  [& args]
  (let [points (parse/points (zipxml (first args)))
        speeds (speeds points)
    ]

     (println (str "Distance: " (format "%.1f" (distance points)) " m"))
     (println (str "Duration: " (t/in-seconds (duration points)) " s"))
     (println)
     (println (str "Max speed: " (format "%.3f" (apply max speeds)) " m/s"))
     (println (str "Avg speed: " (format "%.3f" (average speeds)) " m/s"))
     (println)
     (println (str "Elevation gain: " (format "%d" (elevation-gained points)) " m"))
     (println (str "Elevation loss: " (format "%d" (elevation-lost points)) " m"))
     (println (str "Elevation diff: " (format "%d" (elevation-diff (first points) (last points))) " m"))
     (println)
  ))
