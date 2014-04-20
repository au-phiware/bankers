(ns au.com.phiware.math.binom
    (:refer-clojure :exclude [next seq])
    (:import
      (clojure.lang IDeref)
      (java.lang.ref SoftReference)))

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
  (up!  [_ cell])
  (down [cell] "Returns the cell in the same position as the given cell in
                the preceeding row.")
  (next [cell] "Returns the cell in the succeeding position and succeeding
                row as the given cell.")
  (next![_ cell])
  (back [cell] "Returns the cell in the preceeding position and preceeding
                row as the given cell.")
)

(defn seq [f c] (cons @c (lazy-seq (let [d (f c)] (if d (seq f d))))))

(deftype BinomCellImpl [value n k down-cell back-cell up-cell next-cell]
  IDeref
  (deref [_] value)
  BinomCell
  (row   [_] n)
  (pos   [_] k)
  (down  [_] down-cell)
  (back  [_] back-cell)
  (right [_] (cond (== n k)  nil
                   down-cell (next down-cell)
                   :else     (down (next _))))
  (left  [_] (cond (zero? k) nil
                   back-cell (up back-cell)
                   :else     (back (up _))))
  (sum   [cell] (cond
                  (zero? k) 1
                  (== k n) (bit-shift-left 1 n) 
                  :else (reduce +' (map-indexed #(bit-shift-left (inc %2) (max 0 (dec %1))) (remove #(== 1 %) (seq back cell))))))
  (up    [this] (if (zero? k) nil
                  (or (and @up-cell (.get @up-cell))
                      (let [found (up back-cell)
                            back-up-cell (or found (BinomCellImpl. 1 n (dec k)
                                                                   nil nil nil (atom nil)))
                            cell (BinomCellImpl. (+' value @back-up-cell) (inc n) k
                                                 this back-up-cell (atom nil) (atom nil))]
                        (up! this cell)
                        (if-not found (next! back-up-cell cell))
                        cell))))
  (next  [this] (if (== n k) nil
                  (or (and @next-cell (.get @next-cell))
                      (let [found (next down-cell) 
                            down-next-cell (or found (BinomCellImpl. 1 n (inc k)
                                                                     nil nil nil (atom nil)))
                            cell (BinomCellImpl. (+' value @down-next-cell) (inc n) (inc k)
                                                 down-next-cell this (atom nil) (atom nil))]
                        (next! this cell)
                        (if-not found (up! down-next-cell cell))
                        cell))))
  (up!   [_ cell] (if (and up-cell (not @up-cell)
                           cell (== (row cell) (inc n)) (== (pos cell) k))
                    (swap! up-cell (fn [_] (SoftReference. cell)))))
  (next! [_ cell] (if (and next-cell (not @next-cell)
                           cell (== (row cell) (inc n)) (== (pos cell) (inc k)))
                    (swap! next-cell (fn [_] (SoftReference. cell)))))
  )

(def binom-cell-2-1
     (let [cell1-0 (BinomCellImpl. 1 1 0 nil nil (atom nil) (atom nil)) 
           cell1-1 (BinomCellImpl. 1 1 1 nil nil (atom nil) (atom nil))
           cell2-1 (BinomCellImpl. 2 2 1 cell1-1 cell1-0 (atom nil) (atom nil))]
       (next! cell1-0 cell2-1)
       (up!   cell1-1 cell2-1)
       cell2-1))

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
