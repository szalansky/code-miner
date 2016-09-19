(ns code-miner.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.data.json :as json]))

(defn load-csv [path]
  (with-open [in-file (io/reader path)]
    (doall
     (csv/read-csv in-file))))

(def csv-data (load-csv (str (System/getProperty "user.dir") "/" "logical_coupling.code-maat")))

(defn- grouped-coupling
  "Input: CSV of logical coupling. Returns logical couplings of every file."
  [csv]
  (->> csv
       rest
       (group-by (fn [coupling-vec] (first coupling-vec)))))


(defn file-couplings-map [m]
  (let [f (fn [[file coupling revisions]]
            { :file file :coupling coupling :revisions revisions})
        g (fn [couplings] (into [] (map #(f (rest %1)) couplings)))]
    (into {} (for [[k v] m] [k (g v)]))))

(def jsn (->> csv-data
     (grouped-coupling)
     (file-couplings-map)))

(with-open [w (io/writer "coupling.json" :append true)]
  (.write w (json/write-str jsn)))
