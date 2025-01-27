# csvql

csvql is a Clojure library designed to simplify working with CSV files and performing SQL-like operations on the data.

## Features

- Read CSV files
- Transform CSV headers
- Convert CSV data to a sequence of maps
- Create lookup tables from data
- Perform inner and outer joins on data

## Installation

Add the following dependency to your project.clj:

```clojure
[csvql "0.1.0"]
```

## Usage

### Reading and Processing CSV Files

```clojure
(require '[csvql.core :as csvql])

(def orders-data (csvql/read-csv "dev-resources/order_data.csv"))
;; Or read from a string
(def orders-data (csvql/csv-from-string "a,b,c\n1,2,3"))
;; => '(["a" "b" "c"] ["1" "2" "3"])

(def orders-data-w-kw-headers (csvql/transform-headers data keyword))

;; Convert to a sequence of maps with optional parsing
(def orders (csvql/zip-rows data-with-keyword-headers
                            {:total_amount parse-double
                             :is_shipped parse-boolean}))

;; Equivalent to
(def orders (csvql/read-csv "dev-resources/order_data.csv"
                            (fn [data]
                              (-> data
                                  (csvql/transform-headers keyword)
                                  (csvql/zip-rows {:total_amount parse-double
                                                   :is_shipped parse-boolean})))))
;; =>
'({:order_id "ORD-001",
   :user_id "550e8400-e29b-41d4-a716-446655440000",
   :total_amount 150.75,
   :is_shipped true,
   :order_date "2024-03-15"}
  {:order_id "ORD-002",
   :user_id "f47ac10b-58cc-4372-a567-0e02b2c3d479",
   :total_amount 89.99,
   :is_shipped false,
   :order_date "2024-03-16"}
  ...)
```

### Creating Lookup Tables

```clojure
(csvql/create-lookup orders
                     {:key-fn :order_id
                      :value-fn :total_amount})
;; => 
{"ORD-001" 150.75, "ORD-002" 89.99, "ORD-003" 200.5, "ORD-004" 75.25, "ORD-005" 120.0}
```

### Performing Joins

```clojure
(require '[csvql.join :as join])

(def inner-joined-data (join/inner left-data right-data
                                   {:left-key :user_id
                                    :right-key :id}))

(def outer-joined-data (join/outer left-data right-data
                                   {:left-key :user_id
                                    :right-key :id}))
```

### Putting it All Together

```clojure
(require '[csvql.core :as csvql]
         '[csvql.join :as join])

(def users
  (csvql/read-csv
   "dev-resources/user_data.csv"
   (fn [data]
     (-> data
         (csvql/transform-headers keyword)
         (csvql/zip-rows {:age parse-double
                          :is_active parse-boolean})))))

(def orders
  (csvql/read-csv
   "dev-resources/order_data.csv"
   (fn [data]
     (-> data
         (csvql/transform-headers keyword)
         (csvql/zip-rows {:total_amount parse-double
                          :is_shipped parse-boolean})))))

(join/inner orders users {:left-key :user_id :right-key :id})
;; => 
'({:age 28,
   :name "Alice Smith",
   :is_active true,
   :order_id "ORD-001",
   :is_shipped true,
   :id "550e8400-e29b-41d4-a716-446655440000",
   :user_id "550e8400-e29b-41d4-a716-446655440000",
   :favorite_color "blue",
   :order_date "2024-03-15",
   :total_amount 150.75}
   ...)
```


## API Documentation

### csvql.core

- `read-csv [resource-name-or-path ?processor]`: Reads a CSV file and returns a sequence of rows, applying the `processor` function if provided.
- `read-csv [resource-name-or-path ?processor]`: Parses CSV data from a string and returns a sequence of rows, applying the `processor` function if provided.
- `transform-headers [rows f]`: Transforms the header row by applying the function `f` to each header.
- `zip-rows [contents parsers]`: Converts rows to a sequence of maps, with optional parsing of values.
- `create-lookup [rows {:keys [key-fn value-fn]}]`: Creates a lookup table from a sequence of maps.

### csvql.join

- `inner [left-rows right-rows {:keys [left-key right-key]}]`: Performs an inner join on two sequences of maps.
- `outer [left-rows right-rows {:keys [left-key right-key]}]`: Performs an outer join on two sequences of maps.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
