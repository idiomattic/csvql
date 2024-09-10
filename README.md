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

(def orders-data (csvql/read-csv "test/fixtures/order_data.csv"))

(def orders-data-w-kw-headers (csvql/transform-headers data keyword))

;; Convert to a sequence of maps with optional parsing
(def orders (csvql/zip-rows data-with-keyword-headers
                            {:total_amount parse-double
                             :is_shipped parse-boolean}))

;; Equivalent to
(def orders (csvql/read-zip-parse "test/fixtures/order_data.csv"
                                  {:key-fn keyword
                                   :parsers {:total_amount parse-double
                                             :is_shipped parse-boolean}}))
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
  (csvql/read-zip-parse
   "test/fixtures/user_data.csv"
   {:key-fn keyword
    :parsers {:age parse-long
              :is_active parse-boolean}}))

(def orders
  (csvql/read-zip-parse
   "test/fixtures/order_data.csv"
   {:key-fn keyword
    :parsers {:is_shipped parse-boolean
              :total_amount parse-double}}))

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

- `read-csv [resource-name-or-path]`: Reads a CSV file and returns a sequence of rows.
- `transform-headers [rows f]`: Transforms the header row by applying the function `f` to each header.
- `zip-rows [contents parsers]`: Converts rows to a sequence of maps, with optional parsing of values.
- `read-zip-parse [resource-name-or-path opts]`: Threads resource name/file path through reader and zipper, applying provided opts `key-fn` and `parsers` to the headers and body, respectively.
- `create-lookup [rows {:keys [key-fn value-fn]}]`: Creates a lookup table from a sequence of maps.

### csvql.join

- `inner [left-rows right-rows {:keys [left-key right-key]}]`: Performs an inner join on two sequences of maps.
- `outer [left-rows right-rows {:keys [left-key right-key]}]`: Performs an outer join on two sequences of maps.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
