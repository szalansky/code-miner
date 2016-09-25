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


(defn file-couplings-map
  "Input: a map with the structure described below. Output: a vector of maps described below."
  ;; INPUT { filename1 [[related-file1 coupling1 revisions1] ... [related-fileN couplingN revisions N]] }
  ;; OUTPUT [{:file filename1 :couplings [{:file related-filename1 :coupling-level .. :revisions ..}]}]
  [m]
  (let [f (fn [[file coupling revisions]]
            { :file file :coupling-level coupling :revisions revisions})
        g (fn [couplings] (into [] (map #(f (rest %1)) couplings)))]
    (into [] (for [[k v] m] {:file k :couplings (g v) }))))

(def jsn (->> csv-data
     (grouped-coupling)
     (file-couplings-map)))

(with-open [w (io/writer "coupling.json" :append true)]
  (.write w (json/write-str { :data jsn })))
