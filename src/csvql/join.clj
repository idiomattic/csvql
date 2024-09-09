(ns csvql.join)

(defn inner
  "Performs an inner join on two sequences of maps.
   When given just the `left-key` option, assumes the header to join on is the same
     in both map sequence."
  [left-rows right-rows {:keys [left-key right-key]
                         :or {right-key left-key}}]
  (reduce
   (fn [v row-1]
     (let [join-val (get row-1 left-key)
           matching-rows (filter
                          (fn [row-2]
                            (= join-val (get row-2 right-key)))
                          right-rows)]
       (->> matching-rows
            (map #(merge % row-1))
            (concat v))))
   []
   left-rows))

(defn outer
  "Performs an outer join on two sequences of maps.
   When given just the `left-key` option, assumes the header to join on is the same
     in both map sequence."
  [left-rows right-rows {:keys [left-key right-key]
                         :or {right-key left-key}}]
  (reduce
   (fn [v row-1]
     (let [join-val (get row-1 left-key)
           matching-rows (filter
                          (fn [row-2]
                            (= join-val (get row-2 right-key)))
                          right-rows)]
       (if (seq matching-rows)
         (->> matching-rows
              (map #(merge % row-1))
              (concat v))
         (conj v row-1))))
   []
   left-rows))
