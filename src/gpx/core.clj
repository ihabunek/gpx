(ns gpx.core
  (:gen-class)
  (:require
        [clj-time.core :as t]
        [clj-time.format :as f]
        [clojure.java.io :as io]
        [clojure.xml :as xml]
        [clojure.zip :as zip]
        [gpx.geo :as geo]
        [gpx.parse :as parse]
  ))

; --- Helpers ------------------------------------------------------------------

(defn zipfile [path]
  (let [file (io/file path)]
    (if (.exists file)
      (zip/xml-zip (xml/parse file))
      (throw (Exception. (str "File not found: " path)))
      )))

(defn pairs
  "Takes a collection and returns a list of neighbouring pairs.
   e.g. (1 2 3 4) => ((1 2) (2 3) (3 4))"
  [col]
  (if (< (count col) 2)
    (throw (Exception. "Collection too short, at least 2 elements required")))

  (lazy-seq
    (let [pair (take 2 col)]
      (if (= (count col) 2)
        (list pair)
        (conj (pairs (drop 1 col)) pair) ))))

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
  (let [points (parse/points (zipfile (first args)))
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
