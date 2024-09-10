(ns csvql.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.string :as string]
            [csvql.core :as csvql]))

(def users
  '(["id" "name" "age" "is_active" "favorite_color"]
    ["550e8400-e29b-41d4-a716-446655440000" "Alice Smith" "28" "true" "blue"]
    ["f47ac10b-58cc-4372-a567-0e02b2c3d479" "Bob Johnson" "35" "false" "green"]
    ["7c7e02b5-84b0-4f65-b474-3d91b6e3f7b8" "Charlie Brown" "42" "true" "red"]
    ["9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d" "Diana Lee" "31" "true" "purple"]
    ["3b99e3e0-7598-4bf8-b932-58a356f02307" "Ethan Davis" "39" "false" "orange"]))

(deftest read-csv-test
  (testing "Can read a CSV file that is bundled as a resource."
    (is (= users
           (csvql/read-csv "user_data.csv"))))
  (testing "Can read a CSV file given a file path."
    (is (= users
           (csvql/read-csv "test/fixtures/user_data.csv")))))

(deftest transform-headers-test
  (testing "Can transform headers by applying a function."
    (let [expected-headers [:id :name :age :is_active :favorite_color]]
      (is (= expected-headers
             (-> users
                 (csvql/transform-headers keyword)
                 first))))
    (let [expected-headers ["ID" "NAME" "AGE" "IS_ACTIVE" "FAVORITE_COLOR"]]
      (is (= expected-headers
             (-> users
                 (csvql/transform-headers string/upper-case)
                 first))))))

(deftest zip-rows-test
  (let [expcted-map-seq '({:id "550e8400-e29b-41d4-a716-446655440000",
                           :name "Alice Smith",
                           :age "28",
                           :is_active "true",
                           :favorite_color "blue"}
                          {:id "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                           :name "Bob Johnson",
                           :age "35",
                           :is_active "false",
                           :favorite_color "green"}
                          {:id "7c7e02b5-84b0-4f65-b474-3d91b6e3f7b8",
                           :name "Charlie Brown",
                           :age "42",
                           :is_active "true",
                           :favorite_color "red"}
                          {:id "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
                           :name "Diana Lee",
                           :age "31",
                           :is_active "true",
                           :favorite_color "purple"}
                          {:id "3b99e3e0-7598-4bf8-b932-58a356f02307",
                           :name "Ethan Davis",
                           :age "39",
                           :is_active "false",
                           :favorite_color "orange"})]
    (testing "Can zip rows into maps with header row used as keys."
      (is (= expcted-map-seq
             (-> users
                 (csvql/transform-headers keyword)
                 csvql/zip-rows)))))
  (testing "Can apply parsers to values in rows."
    (let [parsers {:age #(Integer/parseInt %)
                   :is_active #(Boolean/parseBoolean %)}
          result (-> users
                     (csvql/transform-headers keyword)
                     (csvql/zip-rows parsers))]
      (is (every? boolean? (map :is_active result)))
      (is (every? int? (map :age result))))))

(deftest create-lookup-test
  (let [user-data (-> (csvql/read-csv "test/fixtures/user_data.csv")
                      (csvql/transform-headers keyword)
                      (csvql/zip-rows {:age parse-long
                                       :is_active parse-boolean
                                       :total_amount parse-double}))]
    (is (= (csvql/create-lookup user-data {:key-fn :name :value-fn :age})
        {"Alice Smith" 28, "Bob Johnson" 35, "Charlie Brown" 42, "Diana Lee" 31, "Ethan Davis" 39}))))
