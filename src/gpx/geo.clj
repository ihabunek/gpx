(ns gpx.geo
  (:require
    [clojure.algo.generic.math-functions :refer [sin cos atan2 pow sqrt]]))

; Thanks to http://www.movable-type.co.uk/scripts/latlong.html

(def earth-radius 6371009)

(defn deg-rad [deg]
  (-> deg (* Math/PI) (/ 180)))

(defn dist-haversine
  "Uses the haversine formula to calculate the distance between two points

  Haversine formula:
    a = sin²(Δφ/2) + cos φ₁ * cos φ₂ * sin²(Δλ/2)
    c = 2 * atan2( √a, √(1−a) )
    d = R * c

  where φ is latitude in radians,
        λ is longitude in radians,
        R is earth’s radius (mean radius = 6,371km);
  "
  [point1 point2]

  (let [φ₁ (-> point1 :lat deg-rad)
        λ₁ (-> point1 :lon deg-rad)
        φ₂ (-> point2 :lat deg-rad)
        λ₂ (-> point2 :lon deg-rad)
        Δφ (- φ₂ φ₁)
        Δλ (- λ₂ λ₁)

        a (+ (pow (sin (/ Δφ 2)) 2)
             (*
               (cos φ₁)
               (cos φ₂)
               (pow (sin (/ Δλ 2)) 2)))

        c (* 2 (atan2 (sqrt a)
                      (sqrt (- 1 a)))) ]

        (* earth-radius c))
)
