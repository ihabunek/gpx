(ns gpx.util-test
  (:require [clojure.test :refer :all]
            [gpx.util :refer :all]))

(deftest test-formatters

  (testing "test-format-speed"
    (let [speed 12.34]
      (is (= (format-speed speed :mps) "12.34 m/s"))
      (is (= (format-speed speed :kmh) "44.42 km/h"))
      (is (= (format-speed speed :mph) "27.60 mph"))
      (is (= (format-speed speed) "44.42 km/h"))))

  (testing "test-format-distance"
    (is (= (format-distance 0) "0 m"))
    (is (= (format-distance 500) "500 m"))
    (is (= (format-distance 999) "999 m"))
    (is (= (format-distance 1000) "1.00 km"))
    (is (= (format-distance 1001) "1.00 km"))
    (is (= (format-distance 1003) "1.00 km"))
    (is (= (format-distance 1004) "1.00 km"))
    (is (= (format-distance 1005) "1.00 km"))
    (is (= (format-distance 1006) "1.01 km"))
    (is (= (format-distance 1009) "1.01 km"))
    (is (= (format-distance 1010) "1.01 km"))
    (is (= (format-distance 51238) "51.24 km")))

  (testing "test-format-duration"
    (is (= (format-duration 0) "0 sec"))
    (is (= (format-duration 36) "36 sec"))
    (is (= (format-duration 480) "8:00 min"))
    (is (= (format-duration 500) "8:20 min"))
    (is (= (format-duration 999) "16:39 min"))
    (is (= (format-duration 51238) "14:13:58 h"))
    (is (= (format-duration 453785) "5d 6:03:05 h")))
)
