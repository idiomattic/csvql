(ns csvql.join)

(defn inner-join
  "Performs an inner join on two sequences of maps.
   When given one header argument, assumes the header to join on is the same
     in both map sequence."
  ([rows-1 rows-2 header] (inner-join rows-1 rows-2 header header))

  ([rows-1 rows-2 header-1 header-2]
   (reduce
    (fn [v row-1]
      (let [join-val (get row-1 header-1)
            matching-rows (filter
                           (fn [row-2]
                             (= join-val (get row-2 header-2)))
                           rows-2)]
        (->> matching-rows
             (map #(merge % row-1))
             (concat v))))
    []
    rows-1)))

(defn outer-join
  "Performs an outer join on two sequences of maps.
   When given one header argument, assumes the header to join on is the same
     in both map sequence."
  ([rows-1 rows-2 header] (inner-join rows-1 rows-2 header header))

  ([rows-1 rows-2 header-1 header-2]
   (reduce
    (fn [v row-1]
      (let [join-val (get row-1 header-1)
            matching-rows (filter
                           (fn [row-2]
                             (= join-val (get row-2 header-2)))
                           rows-2)]
        (if (seq matching-rows)
          (->> matching-rows
               (map #(merge % row-1))
               (concat v))
          (conj v row-1))))
    []
    rows-1)))

(defn full-outer-join
  "Performs a full outer join on two sequences of maps.
   When given one header argument, assumes the header to join on is the same
     in both map sequence."
  ([rows-1 rows-2 header] (full-outer-join rows-1 rows-2 header header))

  ([rows-1 rows-2 header-1 header-2]
   (let [left (outer-join rows-1 rows-2 header-1 header-2)
         right (outer-join rows-2 rows-1 header-2 header-1)]
     (->> left
          (concat right)
          (map (fn [row]
                 (let [unique-fn (juxt #(get % header-1)
                                       #(get % header-2))]
                   [(unique-fn row) row])))
          (reduce
           (fn [m [unique-on row]]
             (if (contains? m unique-on)
               m
               (assoc m unique-on row)))
           {})
          vals
          vec))))
