(ns gpx.geo
  (:require
    [clojure.math.numeric-tower :as math]))

; Thanks to http://www.movable-type.co.uk/scripts/latlong.html

(def earth-radius 6371009)

(defn deg-rad [deg]
  (-> deg (* Math/PI) (/ 180)))

(defn dist-haversine
  "Uses the haversine formula to calculate the distance between two points

  Haversine formula:
    a = sin²(Δφ/2) + cos φ1 * cos φ2 * sin²(Δλ/2)
    c = 2 * atan2( √a, √(1−a) )
    d = R * c

  where φ is latitude in radians,
        λ is longitude in radians,
        R is earth’s radius (mean radius = 6,371km);
  "
  [point1 point2]

  (let [φ1 (-> point1 :lat deg-rad)
        λ1 (-> point1 :lon deg-rad)
        φ2 (-> point2 :lat deg-rad)
        λ2 (-> point2 :lon deg-rad)
        Δφ (- φ2 φ1)
        Δλ (- λ2 λ1)

        a (+ (math/expt (Math/sin (/ Δφ 2)) 2)
             (*
               (Math/cos φ1)
               (Math/cos φ2)
               (math/expt (Math/sin (/ Δλ 2)) 2)))

        c (* 2 (Math/atan2 (math/sqrt a)
                           (math/sqrt (- 1 a)))) ]

        (* earth-radius c))
)
