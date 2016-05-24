(ns gpx.parse-test
  (:require [clojure.test :refer :all]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [gpx.parse :refer :all]
))

; name in root node
(def gpx-name-1 "
  <gpx>
    <name>Name1</name>
  </gpx>")

; name in trk node
(def gpx-name-2 "
  <gpx>
    <trk>
      <name>Name2</name>
    </trk>
  </gpx>")

; name in metadata node
(def gpx-name-3 "
  <gpx>
    <metadata>
      <name>Name3</name>
    </metadata>
  </gpx>")

; combined names
(def gpx-name-combined "
  <gpx>
    <name>Name1</name>
    <trk><name>Name2</name></trk>
    <metadata><name>Name3</name></metadata>
  </gpx>")


(defn zip-str [s]
  (zip/xml-zip
      (xml/parse (java.io.ByteArrayInputStream. (.getBytes s)))))

(deftest test-parse-name

  (testing "Parsing gpx name from root node"
    (let [gpx (zip-str gpx-name-1)]
      (is (= (parse-name gpx) "Name1"))))

  (testing "Parsing gpx name from trk node"
    (let [gpx (zip-str gpx-name-2)]
      (is (= (parse-name gpx) "Name2"))))

  (testing "Parsing gpx name from metadata node"
    (let [gpx (zip-str gpx-name-3)]
      (is (= (parse-name gpx) "Name3"))))

  (testing "Parsing gpx name priority"
    (let [gpx (zip-str gpx-name-combined)]
      (is (= (parse-names gpx) ["Name1" "Name2" "Name3"]))))

)