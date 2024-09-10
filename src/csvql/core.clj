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

(defn read-zip-parse
  [resource-name-or-path {:keys [key-fn
                                 parsers]
                          :or {key-fn identity
                               parsers {}}}]
  (-> resource-name-or-path
      read-csv
      (transform-headers key-fn)
      (zip-rows parsers)))

(defn create-lookup
  "Creates a lookup table from a sequence of maps."
  [rows {:keys [key-fn value-fn]}]
  (into {} (map (juxt key-fn value-fn) rows)))
