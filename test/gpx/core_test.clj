(ns gpx.core-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [gpx.core :refer :all]
            [gpx.parse :refer :all] ))

(deftest test-stats

  (testing "Stats are correctly combined"
    (let [gpx (parse-gpx-file (io/resource "test/multi-segment.gpx"))
          s1 (-> gpx :segments first stats)
          s2 (-> gpx :segments second stats)
          sc (combine-stats s1 s2)]

      (is (= (-> sc :total :distance) (+ (-> s1 :total :distance) (-> s2 :total :distance))))
      (is (= (-> sc :total :duration) (+ (-> s1 :total :duration) (-> s2 :total :duration))))

      (is (= (-> sc :speed :avg) (/ (+ (-> s1 :speed :avg) (-> s2 :speed :avg)) 2)))
      (is (= (-> sc :speed :max) (max (-> s1 :speed :max) (-> s2 :speed :max))))

      (is (= (-> sc :elevation :gain) (+ (-> s1 :elevation :gain) (-> s2 :elevation :gain))))
      (is (= (-> sc :elevation :loss) (+ (-> s1 :elevation :loss) (-> s2 :elevation :loss))))
      (is (= (-> sc :elevation :diff) (+ (-> s1 :elevation :diff) (-> s2 :elevation :diff)))) )))
