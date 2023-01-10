(ns ch03-sequences.core
  (:require [clojure.string :as str])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(first '(1 2 3))
;; => 1

(rest '(1 2 3))
;; => (2 3)

(cons 0 '(1 2 3))
;; => (0 1 2 3)

(first [1 2 3])
;; => 1

(rest [1 2 3])
;; => (2 3)

(cons 0 [1 2 3])
;; => (0 1 2 3)

(seq? (rest [1 2 3]))
;; => true

(first {:fname "harry" :lname "potter"})
;; => [:fname "harry"]

(rest {:fname "harry" :lname "potter"})
;; => ([:lname "potter"])

(cons [:mname "james"] {:fname "harry" :lname "potter"})
;; => ([:mname "james"] [:fname "harry"] [:lname "potter"])

(first #{:the :quick :brown :fox})
;; => :fox

(rest #{:the :quick :brown :fox})
;; => (:the :quick :brown)

(cons :jumped #{:the :quick :brown :fox})
;; => (:jumped :fox :the :quick :brown)

(sorted-set :the :quick :brown :fox)
;; => #{:brown :fox :quick :the}

(sorted-map :c 3 :b 2 :a 1)
;; => {:a 1, :b 2, :c 3}

(conj '(1 2 3) :a)
;; => (:a 1 2 3)

(into '(1 2 3) '(:a :b :c))
;; => (:c :b :a 1 2 3)

(conj [1 2 3] :a)
;; => [1 2 3 :a]

(into [1 2 3] [:a :b :c])
;; => [1 2 3 :a :b :c]

(range 20)
;; => (0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19)

(range 10 30)
;; => (10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29)

(range 1 25 2)
;; => (1 3 5 7 9 11 13 15 17 19 21 23)

(range 0 -1 -0.25)
;; => (0 -0.25 -0.5 -0.75)

(range 1/2 4 1)
;; => (1/2 3/2 5/2 7/2)

(repeat 5 42)
;; => (42 42 42 42 42)

(repeat 10 "he")
;; => ("he" "he" "he" "he" "he" "he" "he" "he" "he" "he")

(take 10 (iterate inc 1))
;; => (1 2 3 4 5 6 7 8 9 10)

(def whole-numbers (iterate inc 1))

(take 15 (repeat 42))
;; => (42 42 42 42 42 42 42 42 42 42 42 42 42 42 42)

(take 10 (cycle (range 2)))
;; => (0 1 0 1 0 1 0 1 0 1)

(interleave whole-numbers ["a" "b" "c" "d" "e"])
;; => (1 "a" 2 "b" 3 "c" 4 "d" 5 "e")

(interpose "-" ["mango" "banana" "durian"])
;; => ("mango" "-" "banana" "-" "durian")

(apply str (interpose "-" ["mango" "banana" "durian"]))
;; => "mango-banana-durian"

;; or

(str/join "-" ["mango" "banana" "durian"])
;; => "mango-banana-durian"


(take 14 (filter even? whole-numbers))
;; => (2 4 6 8 10 12 14 16 18 20 22 24 26 28)

(take 14 (filter odd? whole-numbers))
;; => (1 3 5 7 9 11 13 15 17 19 21 23 25 27)

(def vowel? #{\a \i \u \e \o})
(def consonant? (complement vowel?))

(take-while consonant? "the-quick-brown-fox")
;; => (\t \h)

(drop-while consonant? "the-quick-brown-fox")
;; => (\e \- \q \u \i \c \k \- \b \r \o \w \n \- \f \o \x)

(split-at 5 (range 8))
;; => [(0 1 2 3 4) (5 6 7)]

(split-with #(<= % 10) (range 0 20 2))
;; => [(0 2 4 6 8 10) (12 14 16 18)]

(every? odd? [1 5 3])
;; => true

(every? odd? [1 2 3])
;; => false

;; some returns actual element value, not boolean
(some even? [1 2 3])
;; => true

(some even? [1 3 5])
;; => nil

(some identity [nil false 1 nil 2])
;; => 1

(some #{5} (range 20))
;; => 5

(some #{10} (range 5))
;; => nil

(not-every? even? whole-numbers)
;; => true

(not-any? even? whole-numbers)
;; => false

(map #(format "<p>%s</p>" %) ["the" "quick" "brown" "fox"])
;; => ("<p>the</p>" "<p>quick</p>" "<p>brown</p>" "<p>fox</p>")

(map #(format "<%s>%s</%s>" %1 %2 %1)
     ["h1" "h2" "h3" "h1"]
     ["the" "quick" "brown" "fox"])
;; => ("<h1>the</h1>" "<h2>quick</h2>" "<h3>brown</h3>" "<h1>fox</h1>")

(reduce + (range 1 11))
;; => 55

(reduce * (range 1 11))
;; => 3628800

(sort [42 1 17 11])
;; => (1 11 17 42)

(sort-by #(.toString %) [42 1 7 11])
;; => (1 11 42 7)

(sort > [42 1 7 11])
;; => (42 11 7 1)

(sort-by :grade > [{:grade 83} {:grade 89} {:grade 77}])
;; => ({:grade 89} {:grade 83} {:grade 77})

(for [word ["the" "quick" "brown" "fox"]]
  (format "<p>%s</p>" word))
;; => ("<p>the</p>" "<p>quick</p>" "<p>brown</p>" "<p>fox</p>")

(take 15 (for [n whole-numbers :when (even? n)] n))
;; => (2 4 6 8 10 12 14 16 18 20 22 24 26 28 30)

(for [n whole-numbers :while (even? n)] n)
;; => ()

(for [file "ABCDEFGH"
      rank (range 1 9)]
  (format "%c%d" file rank))
;; => ("A1" "A2" "A3" "A4" "A5" "A6" "A7" "A8" "B1" "B2" "B3" "B4" "B5" "B6" "B7" "B8" "C1" "C2" "C3" "C4" "C5" "C6" "C7" "C8" "D1" "D2" "D3" "D4" "D5" "D6" "D7" "D8" "E1" "E2" "E3" "E4" "E5" "E6" "E7" "E8" "F1" "F2" "F3" "F4" "F5" "F6" "F7" "F8" "G1" "G2" "G3" "G4" "G5" "G6" "G7" "G8" "H1" "H2" "H3" "H4" "H5" "H6" "H7" "H8")

(def primes
  (concat
   [2 3 5 7]
   (lazy-seq
    (let [primes-from
          (fn primes-from [n [f & r]]
            (if (some #(zero? (rem n %))
                      (take-while #(<= (* % %) n) primes))
              (recur (+ n f) r)
              (lazy-seq (cons n (primes-from (+ n f) r)))))
          wheel (cycle [2 4 2 4 6 2 6 4 2 4 6 6 2 6 4 2
                        6 4 6 8 4 2 4 2 4 8 6 4 6 2 4 6
                        2 6 6 4 2 4 6 2 6 4 2 4 2 10 2 10])]
      (primes-from 11 wheel)))))

(def ordinals-and-primes
  (map vector (iterate inc 1) primes))

(take 5 (drop 1000 ordinals-and-primes))
;; => ([1001 7927] [1002 7933] [1003 7937] [1004 7949] [1005 7951])

(def x (for [i (range 1 3)]
         (do (println i)
             i)))

(doall x)
;; 1
;; 2
;; (1 2)

(def x (for [i (range 1 3)]
         (do (println i)
             i)))
(dorun x)
;; 1
;; 2
;; nil
