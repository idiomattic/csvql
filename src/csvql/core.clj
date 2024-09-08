(ns csvql.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(defn read-csv
  "Given a resource name or path, reads the CSV file and returns a sequence of rows."
  [resource-name-or-path]
  (-> (or (io/resource resource-name-or-path)
          resource-name-or-path)
      slurp
      csv/read-csv))

(defn transform-headers
  "Given a sequence of rows and a function, transforms the header row
     by applying the function to each header."
  [rows f]
  (let [[headers & body] rows]
    (cons (mapv f headers) body)))

(defn zip-rows
  "Given a sequence of rows, including a header row, returns a sequence of maps
     with the respective headers as keys."
  [contents]
  (let [[headers & body] contents]
    (map #(zipmap headers %) body)))

