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

(def default-id-length 6)

(defn random-id
  "Creates a random ID of the given length using alphanumeric characters"
  ([]    (random-id default-id-length))
  ([len] (str/join
            (repeatedly len #(rand-nth alphanumerics)))))

(defn zipxml [path]
  "Loads an XML file from the given path, parse it and creates a zipper."
  (let [file (io/file path)]
    (if (.exists file)
      (zip/xml-zip (xml/parse file))
      (throw (Exception. (str "File not found: " path)))
  )))

(defn pairs
  "Takes a collection and returns a vector of neighbouring pairs.
   e.g. (1 2 3 4) => [(1 2) (2 3) (3 4)]"
  [col]
  (if (empty? (drop 1 col))
    (throw (Exception. "Collection too short, at least 2 elements required")))

  (loop [result []
         s col]
    (if (empty? (drop 2 s))
      (conj result (take 2 s))
      (recur (conj result (take 2 s)) (rest s)))))
