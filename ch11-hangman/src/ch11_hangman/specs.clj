(ns ch11-hangman.specs
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest]
            [ch11-hangman.core :as h]))

(s/def ::letter (set h/letters))

(s/def ::word
  (s/with-gen
    (s/and string?
           #(pos? (count %))
           #(every? h/valid-letter? (seq %)))
    #(gen/fmap
      (fn [letters] (apply str letters))
      (s/gen (s/coll-of ::letter :min-count 1)))))

(s/def ::progress-letter
  (conj (set h/letters) \_))

(s/def ::progress
  (s/coll-of ::progress-letter :min-count 1))

(defn- letters-left [progress]
  (->> progress (keep #{\_}) count))

(s/fdef h/new-progress
  :args (s/cat :word ::word)
  :ret ::progress
  :fn (fn [{:keys [args ret]}]
        (= (count (:word args))
           (count ret)
           (letters-left ret))))

(s/fdef h/update-progress
  :args (s/cat :progress ::progress :word ::word :guess ::letter)
  :ret ::progress
  :fn (fn [{:keys [args ret]}]
        (>= (-> args :progress letters-left)
            (-> ret letters-left))))

(s/fdef h/complete?
  :args (s/cat :progress ::progress :word ::word)
  :ret boolean?)

(defn player? [p]
  (satisfies? h/Player p))

(s/def ::player
  (s/with-gen player?
    #(s/gen #{h/random-player
              h/shuffled-player
              h/alpha-layer
              h/freq-player})))

(s/def ::verbose (s/with-gen boolean? #(s/gen false?)))
(s/def ::score pos-int?)
(s/fdef h/game
  :args (s/cat :word ::word
               :player ::player
               :opts (s/keys* :opt-un [::verbose]))
  :ret ::score)

(defn run-gen []
  (stest/summarize-results
   (stest/check (stest/enumerate-namespace 'ch11-hangman.core))))

(comment
  (gen/sample (s/gen ::word))
  ;; ("vtjzaxyiaszg"
  ;;  "mcdtrggniem"
  ;;  "sjf"
  ;;  "jsdxnlcqwgmpdhgycc"
  ;;  "erdumtaycfhosoavtht"
  ;;  "potuofnh"
  ;;  "husfxiyyspenm"
  ;;  "lzpmxlkmrotle"
  ;;  "qyjzvbvjdkqorf"
  ;;  "dhzxhqzuydafriiy")
  (run-gen)
  ;; {:sym ch11-hangman.core/new-progress}
  ;; {:sym ch11-hangman.core/game}
  ;; {:sym ch11-hangman.core/complete?}
  ;; {:sym ch11-hangman.core/update-progress}
  ;; {:total 4, :check-passed 4
   }
  )
