(ns gpx.db
  (:require
    [clj-time.format :as f]
    [clojure.data.json :as json]
    [config.core :refer [env]]
    [gpx.util :as util]
    [korma.core :refer :all]
    [korma.db :refer :all] )
  (:import org.postgresql.util.PGobject))

(defdb gpxdb
  (postgres (:database env)))

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

(defn transform-track [data]
  (assoc data :meta (from-json (:meta data))
              :stats (from-json (:stats data))))

(defn prepare-track [data]
  (assoc data :meta (to-json (:meta data))
              :stats (to-json (:stats data))))

(defn transform-segment [data]
  (assoc data :path (from-json (:path data))))

(defn prepare-segment [data]
  (assoc data :path (to-json (:path data))))

; --- Entitites ----------------------------------------------------------------

(declare track segment user)

(defentity user
  (has-many track))

(defentity track
  (transform transform-track)
  (prepare prepare-track)
  (belongs-to user)
  (has-many segment))

(defentity segment
  (transform transform-segment)
  (prepare prepare-segment)
  (belongs-to track))

; --- Access functions ---------------------------------------------------------

(defn create-track! [data]
  (let [slug (util/random-id) ; TODO: check if exists
        the-track (insert track
                    (values (-> data
                      (assoc :slug slug)
                      (dissoc :id :points))))
        track-id (:id the-track)
        the-segment (insert segment
                      (values { :track_id track-id
                                :path (:points data)}))]
        the-track
))

(defn fetch-track [slug]
  (first (select track
    (with segment)
    (where { :slug slug } )
    (limit 1))))
