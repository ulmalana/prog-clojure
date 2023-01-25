(ns ch11-hangman.core
  (:require [clojure.java.io :as jio]
            [clojure.string :as str])
  (:gen-class))

(defonce letters
  (mapv char (range (int \a) (inc (int \z)))))

(defn rand-letter []
  (rand-nth letters))

;; (defn next-guess [player progress])
(defprotocol Player
  (next-guess [player progress]))

(def random-player
  (reify Player
    (next-guess [_ progress]
      (rand-letter))))

(defn new-progress [word]
  (repeat (count word) \_))

(defn update-progress [progress word guess]
  (map #(if (= %1 guess) guess %2) word progress))

(defn complete? [progress word]
  (= progress (seq word)))

(defrecord ChoicesPlayer [choices]
  Player
  (next-guess [_ progress]
    (let [guess (first @choices)]
      (swap! choices rest)
      guess)))

(defn choices-player [choices]
  (->ChoicesPlayer (atom choices)))

(defn shuffled-player []
  (choices-player (shuffle letters)))

(defn alpha-layer []
  (choices-player letters))

(defn freq-player []
  (choices-player (seq "etaoinshrdlcumwfgypbvkjxqz")))

(defn valid-letter? [c]
  (<= (int \a) (int c) (int \z)))

(defonce available-words
  (with-open [r (jio/reader "resources/words.txt")]
    (->> (line-seq r)
         (filter #(every? valid-letter? %))
         vec)))

(defn rand-word []
  (rand-nth available-words))

(defn report [begin-progress guess end-progress]
  (println)
  (println "you guessed:" guess)
  (if (= begin-progress end-progress)
    (if (some #{guess} end-progress)
      (println "sorry you already guessed:" guess)
      (println "sorry the word doesnt contain:" guess))
    (println "the letter" guess "is in the word"))
  (println "progress so far:" (apply str end-progress)))

(defn game
  [word player & {:keys [verbose] :or {verbose false}}]
  (when verbose
    (println "you are guessing a word with" (count word) "letters"))
  (loop [progress (new-progress word), guesses 1]
    (let [guess (next-guess player progress)
          progress' (update-progress progress word guess)]
      (when verbose (report progress guess progress'))
      (if (complete? progress' word)
        guesses
        (recur progress' (inc guesses))))))


(defn take-guess []
  (println)
  (print "enter a letter: ")
  (flush)
  (let [input (.readLine *in*)
        line (str/trim input)]
    (cond
      (str/blank? line) (recur)
      (valid-letter? (first line)) (first line)
      :else (do
              (println "that is not a valid letter")
              (recur)))))

(def interactive-player
  (reify Player
    (next-guess [_ progress]
      (take-guess))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (game (rand-word) interactive-player :verbose true))
