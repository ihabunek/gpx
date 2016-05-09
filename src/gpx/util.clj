(ns gpx.util
    (:require [clojure.string :as str]))

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
