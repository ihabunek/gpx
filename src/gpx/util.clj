(ns gpx.util
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [clojure.xml :as xml]
    [clojure.zip :as zip]
))

(def alphanumerics (map char
    (concat
        (range 48 57)     ; 0-9
        (range 65 90)     ; A-Z
        (range 97 122)))) ; a-z

(defn random-id
  "Creates a random ID of the given length using alphanumeric characters"
  ([]    (random-id 6))
  ([len] (str/join
            (repeatedly len #(rand-nth alphanumerics)))))

(defn zipxml [path]
  "Loads an XML file from the given path, parse it and creates a zipper."
  (let [file (io/file path)]
    (if (.exists file)
      (zip/xml-zip (xml/parse file))
      (throw (Exception. (str "File not found: " path)))
  )))

(defn format-distance [value]
  (if (< value 1000)
    (str (int value) " m")
    (str (format "%.2f" (float (/ value 1000))) " km")))

(defn format-duration [duration]
  (let [days (-> duration (/ 60 60 24) int)
        hours (-> duration (/ 60 60) (rem 24) int)
        minutes (-> duration (/ 60) (rem 60) int)
        seconds (-> duration (rem 60))]
    (cond
      (< duration 60)           (format "%d sec" seconds)
      (< duration (* 60 60))    (format "%d:%02d min" minutes seconds)
      (< duration (* 60 60 24)) (format "%d:%02d:%02d h" hours minutes seconds)
      :else                     (format "%dd %d:%02d:%02d h" days hours minutes seconds)
    )))

(defn format-speed
  "Takes the speed in m/s and formats it in one of target units
  (:kmh :mps :mph). Defaults to :kmh"
  ([value] (format-speed value :kmh))
  ([value fmt]
    (let [speed (float value)]
      (cond
        (= fmt :mps) (format "%.2f m/s" speed)
        (= fmt :kmh) (format "%.2f km/h" (* speed 3.6))
        (= fmt :mph) (format "%.2f mph" (* speed 2.23694))
        :else value ))))
