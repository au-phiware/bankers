(ns au.com.phiware.math.binom
    (:refer-clojure :exclude [next seq])
    (:import
      (clojure.lang IDeref)))

(defprotocol BinomCell
  "Represents a cell in Pascal's Triangle, where the cell's value
   contributes to the `next` cell and the `up` cell.
   This protocol provides functions to navigate from one cell to the next cell
   or the upward cell and conversely from the next cell back to the cell or
   from the up cell down to the cell. Navigation with in a row, left and right,
   is also possible."
  (row  [cell] "Returns the row of the given cell.")
  (pos  [cell] "Returns the position of the given cell.")
  (sum  [cell] "Returns the sum of the cells from the beggining of the row
                up to and including the given cell.")
  (right[cell] "Returns the succeeding cell in the same row as the given
                cell.")
  (left [cell] "Returns the preceeding cell in the same row as the given
                cell.")
  (up   [cell] "Returns the cell in the same position as the given cell in
                the succeeding row.")
  (down [cell] "Returns the cell in the same position as the given cell in
                the preceeding row.")
  (next [cell] "Returns the cell in the succeeding position and succeeding
                row as the given cell.")
  (back [cell] "Returns the cell in the preceeding position and preceeding
                row as the given cell.")
)

(extend-type nil BinomCell
             (up    [_] nil)
             (down  [_] nil)
             (next  [_] nil)
             (back  [_] nil)
             (left  [_] nil)
             (right [_] nil))

(declare binom binom-cell-1-0 binom-cell-1-1 binom-cell-2-1)

(defn seq
  ([]
   (concat [binom-cell-1-0 binom-cell-1-1] (lazy-seq (seq binom-cell-2-1))))
  ([cell]
   (cons cell (lazy-seq (seq (or (right cell) (binom (inc (row cell)) 0))))))
  ([f c]
   (cons @c (lazy-seq (let [d (f c)] (if d (seq f d)))))))

(defn- soft-ref-biulder [cell] (fn [& _] (java.lang.ref.SoftReference. cell)))
(defn- soft-ref-get [o] (.get o))
;(defn- soft-ref-biulder [cell] (constantly cell)) (defn- soft-ref-get [o] o)

(deftype BinomCellImpl [value n k down-cell back-cell up-cell next-cell]
  IDeref
  (deref [_] value)
  BinomCell
  (row   [_] n)
  (pos   [_] k)
  (down  [_] down-cell)
  (back  [_] back-cell)
  (right [this] (cond (== n k)  nil
                      (== n 1)  binom-cell-1-1
                      :else     (next (down this))))
  (left  [this] (cond (zero? k) nil
                      (== n 1)  binom-cell-1-0
                      :else     (up (back this))))
  (sum   [cell] (cond
                  (zero? k) 1
                  (== k n)  (bit-shift-left 1 n)
                  :else     (reduce +' (map-indexed
                                         #(bit-shift-left (inc %2) (max 0 (dec %1)))
                                         (remove #(== 1 %) (seq back cell))))))
  (up    [this] (or (and @up-cell (soft-ref-get @up-cell))
                    (let [left-cell (left this)
                          cell (BinomCellImpl.
                                 (if left-cell (+' value @left-cell) 1)
                                 (inc n) k
                                 this left-cell (atom nil) (atom nil))]
                      (swap! up-cell (soft-ref-biulder cell))
                      (if left-cell (swap! (.next-cell left-cell) (soft-ref-biulder cell)))
                      cell)))
  (next  [this] (or (and @next-cell (soft-ref-get @next-cell))
                    (let [right-cell (right this)
                          cell (BinomCellImpl.
                                 (if right-cell (+' value @right-cell) 1)
                                 (inc n) (inc k)
                                 right-cell this (atom nil) (atom nil))]
                      (swap! next-cell (soft-ref-biulder cell))
                      (if right-cell (swap! (.up-cell right-cell) (soft-ref-biulder cell)))
                      cell)))
)

(def binom-cell-1-0 (BinomCellImpl.
                        1 1 0
                        nil nil
                        (atom nil) (atom nil)))
(def binom-cell-1-1 (BinomCellImpl.
                        1 1 1
                        nil nil
                        (atom nil) (atom nil)))
(def binom-cell-2-1 (up binom-cell-1-1))

(defn binom
  "Returns a cell from Pascal's triangle at the given row and position."
  [n k]
  (cond
    (some neg? [(dec n) k (- n k)]) nil
    (zero? k) (back (binom (inc n) (inc k)))
    (== n k)  (down (binom (inc n) k))
    (== n 2)  binom-cell-2-1
    :else     (reduce (fn [cell k]
                          (if (< (pos cell) k)
                            (next cell)
                            (up   cell)))
                      binom-cell-2-1
                      (drop 2 (take n (iterate #(min (inc %) k) 0))))))
