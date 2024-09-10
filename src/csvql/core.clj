(ns csvql.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]))


(defn read-csv
  "Given a resource name or path, reads the CSV file and returns a lazy sequence of rows.
   Optionally accepts a processing function to apply to the rows."
  ([resource-name-or-path]
   (read-csv resource-name-or-path identity))

  ([resource-name-or-path processor]
   (let [resource (or (io/resource resource-name-or-path)
                      resource-name-or-path)]
     (with-open [reader (io/reader resource)]
       (doall (processor (csv/read-csv reader)))))))

(defn csv-from-string
  "Parses CSV data from a string."
  ([s]
   (csv/read-csv (java.io.StringReader. s)))
  ([s processor]
   (processor (csv/read-csv (java.io.StringReader. s)))))

(defn transform-headers
  "Given a sequence of rows and a function, transforms the header row
     by applying the function to each header."
  [rows f]
  (let [[headers & body] rows]
    (cons (mapv f headers) body)))

(defn zip-rows
  "Given a sequence of rows, including a header row, returns a sequence of maps
     with the respective headers as keys."
  ([contents]
   (zip-rows contents {}))

  ([contents parsers]
   (let [[headers & body] contents]
     (map
      (fn [row]
        (reduce (fn [acc [k v]]
                  (let [parser (get parsers k identity)]
                    (assoc acc k (parser v))))
                {}
                (map vector headers row)))
      body))))

(defn create-lookup
  "Creates a lookup table from a sequence of maps."
  [rows {:keys [key-fn value-fn]}]
  (into {} (map (juxt key-fn value-fn) rows)))
