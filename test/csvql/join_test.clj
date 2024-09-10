(ns csvql.join-test
  (:require [clojure.test :refer [deftest testing is]]
            [csvql.core :as csvql]
            [csvql.join :as join]))

(def users (-> (csvql/read-csv "dev-resources/user_data.csv")
               (csvql/transform-headers keyword)
               csvql/zip-rows))

(def orders (-> (csvql/read-csv "dev-resources/order_data.csv")
                (csvql/transform-headers keyword)
                csvql/zip-rows))

(deftest inner-test
  (testing "can perform an inner join on two sequences of maps"
    (is (= '({:age "28",
              :name "Alice Smith",
              :is_active "true",
              :order_id "ORD-001",
              :is_shipped "true",
              :id "550e8400-e29b-41d4-a716-446655440000",
              :user_id "550e8400-e29b-41d4-a716-446655440000",
              :favorite_color "blue",
              :order_date "2024-03-15",
              :total_amount "150.75"}
             {:age "35",
              :name "Bob Johnson",
              :is_active "false",
              :order_id "ORD-002",
              :is_shipped "false",
              :id "f47ac10b-58cc-4372-a567-0e02b2c3d479",
              :user_id "f47ac10b-58cc-4372-a567-0e02b2c3d479",
              :favorite_color "green",
              :order_date "2024-03-16",
              :total_amount "89.99"}
             {:age "31",
              :name "Diana Lee",
              :is_active "true",
              :order_id "ORD-004",
              :is_shipped "true",
              :id "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
              :user_id "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
              :favorite_color "purple",
              :order_date "2024-03-18",
              :total_amount "75.25"})
           (join/inner users orders {:left-key :id
                                     :right-key :user_id})))))

(deftest outer-test
  (testing "can perform an outer join on two sequences of maps"
    (is (= '({:id "3b99e3e0-7598-4bf8-b932-58a356f02307",
              :name "Ethan Davis",
              :age "39",
              :is_active "false",
              :favorite_color "orange"}
             {:id "7c7e02b5-84b0-4f65-b474-3d91b6e3f7b8",
              :name "Charlie Brown",
              :age "42",
              :is_active "true",
              :favorite_color "red"}
             {:age "28",
              :name "Alice Smith",
              :is_active "true",
              :order_id "ORD-001",
              :is_shipped "true",
              :id "550e8400-e29b-41d4-a716-446655440000",
              :user_id "550e8400-e29b-41d4-a716-446655440000",
              :favorite_color "blue",
              :order_date "2024-03-15",
              :total_amount "150.75"}
             {:age "35",
              :name "Bob Johnson",
              :is_active "false",
              :order_id "ORD-002",
              :is_shipped "false",
              :id "f47ac10b-58cc-4372-a567-0e02b2c3d479",
              :user_id "f47ac10b-58cc-4372-a567-0e02b2c3d479",
              :favorite_color "green",
              :order_date "2024-03-16",
              :total_amount "89.99"}
             {:age "31",
              :name "Diana Lee",
              :is_active "true",
              :order_id "ORD-004",
              :is_shipped "true",
              :id "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
              :user_id "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
              :favorite_color "purple",
              :order_date "2024-03-18",
              :total_amount "75.25"})
            (join/outer users orders {:left-key :id
                                      :right-key :user_id})))))
