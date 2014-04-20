(ns au.com.phiware.math.binom-test
  (:require [clojure.test :refer :all]
            [clojure.test.check :refer :all]
            [clojure.test.check.clojure-test :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [au.com.phiware.math.binom :as binom :refer [binom]]))

(defn ! [n] (reduce *' (range 1 (inc n))))

(deftest known-values
  (testing "binom"
    (testing "first 15 rows"
      (defn choose [n k]
        (let [b (cond 
                  (zero? n) 0
                  (or (zero? k) (== n k)) 1
                  :else (+ (choose (dec n) (dec k)) (choose (dec n) k)))]
          (is (= b @(binom n k)))
          b))
      (defn sum [n k]
        (let [s (if (zero? k) 1
                    (+ (sum n (dec k)) (choose n k)))]
          (is (= s (binom/sum (binom n k))))
          s))
      (sum 15 15))

    (testing "with large values"
      (is (= 600805296 @(binom 36 11)))  
      (is (= 9075135300 @(binom 36 18)))
      (is (= 135107941996194268514474877978504530397233945449193479925965721786474150408005716961950480198274469818673334131365837249043900490761151591695308427048536947621976068789875968372656 @(binom 600N 300N))))))

(defspec
  binom-matches-formula
  (prop/for-all [args (gen/bind gen/s-pos-int #(gen/tuple (gen/return %) (gen/choose 0 %)))]
                (let [[n k] args]
                  (= @(apply binom args)
                     (/ (! n) (*' (! k) (! (- n k))))))))

(comment defspec
  binom-from-any-path-is-identical
  (prop/for-all [path (gen/such-that not-empty (gen/list (gen/elements ['au.com.phiware.math.binom/up 'au.com.phiware.math.binom/next])))]
                (let [b (eval (reduce #(list %2 %1) (binom 2 1) path))]
                  (= b (binom (binom/row b) (binom/pos b))))))
