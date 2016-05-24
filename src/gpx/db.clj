(ns gpx.db
  (:require
    [clj-time.format :as f]
    [clj-time.coerce :as c]
    [clj-postgresql.core :as pg]
    [clojure.data.json :as json]
    [clojure.java.jdbc :as jdbc]
    [clojure.pprint :refer [pprint]]
    [clojure.string :as str]
    [config.core :refer [env]]
    [gpx.util :as util]
    [korma.core :refer :all]
    [korma.db :refer :all]
  )
  (:import org.postgresql.util.PGobject))

(defdb gpxdb
  (postgres (:database env)))

(defn pg-array [type data]
  (.createArrayOf
    (jdbc/get-connection (get-connection gpxdb)) type (into-array data)))

; Types defined here:
; http://grepcode.com/file/repo1.maven.org/maven2/postgresql/postgresql/9.1-901.jdbc4/org/postgresql/jdbc2/TypeInfoCache.java/#71

(defn to-real-array [data]
  (pg-array "float4" data))

(defn to-double-array [data]
  (pg-array "float8" data))

(defn to-ts-array [data]
  (pg-array "timestamptz" data))

; --- Converters ---------------------------------------------------------------

(def formatter (f/formatters :date-time))

(defn value-writer [key value]
  (if (= key :time) (f/unparse formatter value) value))

(defn value-reader [key value]
  (if (= key :time) (f/parse formatter value) value))

(defn to-json [data]
  (doto (PGobject.)
      (.setType "json")
      (.setValue
        (json/write-str data :value-fn value-writer))))

(defn from-json [pg-object]
  (json/read-str (.getValue pg-object) :value-fn value-reader :key-fn keyword))

; --- Transform / prepare ------------------------------------------------------

(defn transform-track [data]
  (assoc data :metadata (from-json (:metadata data))
              :stats (from-json (:stats data))))

(defn prepare-track [data]
  (assoc data :metadata (to-json (:metadata data))
              :stats (to-json (:stats data))))

(defn transform-segment [data]
  (let [times (map c/from-sql-time (:times data))]
    (-> data
      (assoc :times times)
      (assoc :points
        (map #(zipmap [:lat :lon :ele :time] %)
          (map vector (:lats data) (:lons data) (:elevations data) times)))
      (dissoc :lats :lons :elevations :times))))

(defn prepare-segment [data]
  (assoc data :lats (to-double-array (:lats data))
              :lons (to-double-array (:lons data))
              :elevations (to-real-array (:elevations data))
              :times (to-ts-array (map #(f/unparse formatter %) (:times data)))))

(defn prepare-waypoint [data]
  (assoc data :time (c/to-sql-time (:time data))))

; --- Entitites ----------------------------------------------------------------

(declare track segment user waypoint)

(defentity user
  (has-many track))

(defentity track
  ; (transform transform-track) ; not needed when using clj-postgresql
  (prepare prepare-track)
  (belongs-to user)
  (has-many segment)
  (has-many waypoint))

(defentity segment
  (transform transform-segment)
  (prepare prepare-segment)
  (belongs-to track))

(defentity waypoint
  (prepare prepare-waypoint)
  (belongs-to track))

; --- Access functions ---------------------------------------------------------

(defn create-segment! [track-id lats lons elevations times]
  (insert segment
    (values { :track_id track-id
              :lats lats
              :lons lons
              :elevations elevations
              :times times })))

(defn create-track! [name metadata stats]
  (let [slug (util/random-id)] ; TODO: check if exists
    (insert track
      (values { :slug slug
                :name name
                :metadata metadata
                :stats stats } ))))

(defn create-waypoint! [track-id name lat lon elevation time]
  (insert waypoint
    (values { :track_id track-id
              :name name
              :lat lat
              :lon lon
              :elevation elevation
              :time time })))

(defn fetch-track [slug]
  (first (select track
    (with segment)
    (with waypoint)
    (where { :slug slug } )
    (limit 1))))
