(ns ch02-explore.core
  (:require [clojure.string :as str])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(defrecord Book [title author])

(->Book "Silmarilion" "Tolkien")
;; => #ch02_explore.core.Book{:title "Silmarilion", :author "Tolkien"}

(->Book "Harry Potter" "Rowling")
;; => #ch02_explore.core.Book{:title "Harry Potter", :author "Rowling"}

(str \h \e \y \space \y \o \u)
;; => "hey you"

;; empty list '() is not false
(if ()
  "() is true"
  "() is false")
;; => "() is true"

(defn greeting
  ([] (greeting "world"))
  ([uname] (str "Halo, " uname)))

(defn date [p1 p2 & chaperones]
  (println p1 "and" p2 "went out with"
           (count chaperones) "chaperones"))


(date "Romeo" "Juliet" "Lawrence" "Nurse")
;; Romeo and Juliet went out with 2 chaperones
;; => nil

(defn indexable-word? [word]
  (> (count word) 2))

(filter indexable-word? (str/split "A fine day it is" #"\W+"))
;; => ("fine" "day")

(filter (fn [w] (> (count w) 2)) (str/split "A fine day" #"\W+"))
;; => ("fine" "day")

(defn indexable-words [text]
  (let [indexable-word? (fn [w] (> (count w) 2))]
    (filter indexable-word? (str/split text #"\W+"))))

(indexable-words "a fine date it is")
;; => ("fine" "date")

(defn make-greeter [prefix]
  (fn [uname] (str prefix ", " uname)))

(def hello-greeting (make-greeter "Hello"))

(def aloha-greeting (make-greeter "Aloha"))

(hello-greeting "riz")
;; => "Hello, riz"

(aloha-greeting "riz")
;; => "Aloha, riz"

((make-greeter "howdy") "tim")
;; => "howdy, tim"

(defn square-corners
  [bottom left size]
  (let [top (+ bottom size)
        right (+ left size)]
    [[bottom left] [top left] [top right] [bottom right]]))

(square-corners 2 2 2)
;; => [[2 2] [4 2] [4 4] [2 4]]

(defn ellipsize [words]
  (let [[w1 w2 w3] (str/split words #"\s+")]
    (str/join " " [w1 w2 w3 "..."])))

(ellipsize "the quick brown fox jumps over the lazy dog")
;; => "the quick brown ..."

(defn ^{:tag String} shout [^{:tag String} s]
  (str/upper-case s))

(meta #'shout)
;; => {:tag java.lang.String, :arglists ([s]), :line 88, :column 1, :file "/home/riz/prog-clojure/ch02-explore/src/ch02_explore/core.clj", :name shout, :ns #namespace[ch02-explore.core]}

(defn ^String shout' [^String s]
  (str/upper-case s))

(defn shout''
  ([s] (str/upper-case s))
  {:tag String})

(def rnd (java.util.Random.))

(. rnd nextInt)
;; => 45976886

(. rnd nextInt 42)
;; => 25
;; => 0

(def p (java.awt.Point. 10 20))

(. p x)
;; => 10

(. p -x)
;; => 10

(.-x p)
;; => 10

(defn triple [number]
  #_(println "debug triple" number)
  (* 3 number))

(defn is-small? [number]
  (if (< number 100)
    "yes"
    "no"))

(defn is-small?' [number]
  (if (< number 100)
    "yes"
    (do
      (println "saw a big number" number)
      "no")))

(is-small? 200)
;; => "no"

(is-small?' 200)
;; saw a big number 200
;; => "no"

(loop [result []
       x 5]
  (if (zero? x)
    result
    (recur (conj result x) (dec x))))
;; => [5 4 3 2 1]

(defn countdown [result x]
  (if (zero? x)
    result
    (recur (conj result x) (dec x))))

(countdown [] 5)
;; => [5 4 3 2 1]

(defn indexed [coll]
  (map-indexed vector coll))

(indexed "abcde")
;; => ([0 \a] [1 \b] [2 \c] [3 \d] [4 \e])

(defn index-filter [pred coll]
  (when pred
    (for [[idx elt] (indexed coll) :when (pred elt)]
      idx)))

(index-filter #{\a \b} "abcdbbb")
;; => (0 1 4 5 6)

(index-filter #{\a \b} "xy z")
;; => ()

(defn index-of-any [pred coll]
  (first (index-filter pred coll)))

(index-of-any #{\z \a} "zzabyysfksk")
;; => 0

(index-of-any #{\b \y} "zzabyydccdxx")
;; => 3

(nth
 (index-filter #{:h} [:t :t :h :t :h :t :t :t :t :h :h])
 2)
;; => 9
