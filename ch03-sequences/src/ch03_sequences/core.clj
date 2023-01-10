(ns ch03-sequences.core
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.set :as s])
  (:import [java.io File])
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

(first (.getBytes "hello"))
;; => 104

(rest (.getBytes "hello"))
;; => (101 108 108 111)

(cons (int \h) (.getBytes "ello"))
;; => (104 101 108 108 111)

(first (System/getProperties))
;; => #object[java.util.concurrent.ConcurrentHashMap$MapEntry 0xf7662f9 "awt.toolkit=sun.awt.X11.XToolkit"]

(first "hello")
;; => \h

(rest "hello")
;; => (\e \l \l \o)

(cons \h "ello")
;; => (\h \e \l \l \o)

(reverse "hello")
;; => (\o \l \l \e \h)

(apply str (reverse "hello"))
;; => "olleh"

(re-seq #"\w+" "the quick brown fox")
;; => ("the" "quick" "brown" "fox")

(sort (re-seq #"\w+" "the quick brown fox"))
;; => ("brown" "fox" "quick" "the")

(drop 2 (re-seq #"\w+" "the quick brown fox"))
;; => ("brown" "fox")

(map str/upper-case (re-seq #"\w+" "the quick brown fox"))
;; => ("THE" "QUICK" "BROWN" "FOX")

(.listFiles (File. "."))
;; => #object["[Ljava.io.File;" 0x47127fd3 "[Ljava.io.File;@47127fd3"]

(seq (.listFiles (File. ".")))
;; => (#object[java.io.File 0x7e70e741 "./.hgignore"] #object[java.io.File 0x7e8154f8 "./test"] #object[java.io.File 0x14cbf97a "./README.md"] #object[java.io.File 0x7f966f12 "./CHANGELOG.md"] #object[java.io.File 0x6a5c54e "./.gitignore"] #object[java.io.File 0x727777f6 "./project.clj"] #object[java.io.File 0x39bc40eb "./doc"] #object[java.io.File 0x759e5631 "./resources"] #object[java.io.File 0x5d397279 "./target"] #object[java.io.File 0x14b866ef "./.nrepl-port"] #object[java.io.File 0x4fd8d78b "./LICENSE"] #object[java.io.File 0x48f02f9f "./src"])

(map #(.getName %) (.listFiles (File. ".")))
;; => (".hgignore" "test" "README.md" "CHANGELOG.md" ".gitignore" "project.clj" "doc" "resources" "target" ".nrepl-port" "LICENSE" "src")

(count (file-seq (File. ".")))
;; => 28

(defn minutes-to-millis [mins]
  (* mins 1000 60))

(defn recently-modified? [file]
  (> (.lastModified file)
     (- (System/currentTimeMillis) (minutes-to-millis 30))))

(filter recently-modified? (file-seq (File. ".")))
;; => (#object[java.io.File 0x25c17498 "."] #object[java.io.File 0x738a5bce "./target"] #object[java.io.File 0x5da4a811 "./target/default"] #object[java.io.File 0x2cfcfa85 "./target/default/classes"] #object[java.io.File 0x54602b2b "./target/default/classes/META-INF"] #object[java.io.File 0x659f865b "./target/default/classes/META-INF/maven"] #object[java.io.File 0x24f139a5 "./target/default/classes/META-INF/maven/ch03-sequences"] #object[java.io.File 0x6f92d76c "./target/default/classes/META-INF/maven/ch03-sequences/ch03-sequences"] #object[java.io.File 0x3a7e80c6 "./target/default/classes/META-INF/maven/ch03-sequences/ch03-sequences/pom.properties"] #object[java.io.File 0x23ca748c "./target/default/stale"] #object[java.io.File 0xedb5d41 "./target/default/stale/leiningen.core.classpath.extract-native-dependencies"] #object[java.io.File 0x778bc241 "./target/default/repl-port"] #object[java.io.File 0x393d04c6 "./.nrepl-port"] #object[java.io.File 0x49236979 "./src/ch03_sequences"] #object[java.io.File 0x1dc61449 "./src/ch03_sequences/core.clj"])

(take 2 (line-seq (io/reader "README.md")))
;; => ("# ch03-sequences" "")

(with-open [rdr (io/reader "README.md")]
  (count (line-seq rdr)))
;; => 44

;; count non blank lines in a file
(with-open [rdr (io/reader "README.md")]
  (count (filter #(re-find #"\S" %) (line-seq rdr))))
;; => 27

(defn non-blank? [line]
  (not (str/blank? line)))

(defn non-svn? [file]
  (not (.contains (.toString file) ".svn")))

(defn clojure-source? [file]
  (.endsWith (.toString file) ".clj"))

(defn clojure-loc [base-file]
  (reduce
   +
   (for [file (file-seq base-file)
         :when (and (clojure-source? file) (non-svn? file))]
     (with-open [rdr (io/reader file)]
       (count (filter non-blank? (line-seq rdr)))))))

(clojure-loc (File. "."))
;; => 251

(peek '(1 2 3))
;; => 1

(pop '(1 2 3))
;; => (2 3)

(peek [1 2 3])
;; => 3

(pop [1 2 3])
;; => [1 2]

(assoc [0 1 2 3 4 5] 2 :two)
;; => [0 1 :two 3 4 5]

;; subvec is much faster and specific for vector
;; while take and drop is general for any sequences.
(subvec [1 2 3 4 5] 3)
;; => [4 5]

(subvec [1 2 3 4 5] 1 3)
;; => [2 3]

(take 2 (drop 1 [1 2 3 4 5]))
;; => (2 3)

(def score {:luffy nil :zoro 70})

(:luffy score)
;; => nil

(get score :luffy :score-not-found)
;; => nil

(get score :sanji :not-found)
;; => :not-found

(def song {:name "Agnus Dei"
           :artist "Penderecki"
           :album "Polish Requiem"
           :genre "Classical"})

(assoc song :kind "MPEG audio")
;; => {:name "Agnus Dei", :artist "Penderecki", :album "Polish Requiem", :genre "Classical", :kind "MPEG audio"}

(dissoc song :genre)
;; => {:name "Agnus Dei", :artist "Penderecki", :album "Polish Requiem"}

(select-keys song [:name :artist])
;; => {:name "Agnus Dei", :artist "Penderecki"}

(merge song {:size 12345 :time 32456})
;; => {:name "Agnus Dei", :artist "Penderecki", :album "Polish Requiem", :genre "Classical", :size 12345, :time 32456}

(merge-with
 concat
 {:rubble ["Barney"] :flintstone ["Fred"]}
 {:rubble ["Betty"] :flintstone ["Wilma"]}
 {:rubble ["Bambam"] :flintstone ["Pebles"]})
;; => {:rubble ("Barney" "Betty" "Bambam"), :flintstone ("Fred" "Wilma" "Pebles")}

(def languages #{"java" "c" "d" "clojure"})
(def beverages #{"java" "chai" "pop"})

(s/union languages beverages)
;; => #{"d" "clojure" "pop" "java" "chai" "c"}

(s/difference languages beverages)
;; => #{"d" "clojure" "c"}

(s/intersection languages beverages)
;; => #{"java"}

(s/select #(= 1 (count %)) languages)
;; => #{"d" "c"}

(def compositions
  #{{:name "The art of the fugue" :composer "Bach"}
    {:name "Musical offering" :composer "Bach"}
    {:name "Requiem" :composer "Verdi"}
    {:name "Requiem" :composer "Mozart"}})

(def composers
  #{{:composer "Bach" :country "Germany"}
    {:composer "Mozart" :country "Austria"}
    {:composer "Verdi" :country "Italy"}})

(def nations
  #{{:nation "Germany" :language "German"}
    {:nation "Austria" :language "German"}
    {:nation "Italy" :language "Italian"}})

(s/rename compositions {:name :title})
;; => #{{:composer "Verdi", :title "Requiem"} {:composer "Bach", :title "The art of the fugue"} {:composer "Mozart", :title "Requiem"} {:composer "Bach", :title "Musical offering"}}

(s/select #(= (:name %) "Requiem") compositions)
;; => #{{:name "Requiem", :composer "Mozart"} {:name "Requiem", :composer "Verdi"}}

(s/project compositions [:name])
;; => #{{:name "Musical offering"} {:name "Requiem"} {:name "The art of the fugue"}}

(for [m compositions
      c composers]
  (concat m c))
;; => (([:name "Requiem"] [:composer "Mozart"] [:composer "Bach"] [:country "Germany"]) ([:name "Requiem"] [:composer "Mozart"] [:composer "Mozart"] [:country "Austria"]) ([:name "Requiem"] [:composer "Mozart"] [:composer "Verdi"] [:country "Italy"]) ([:name "Requiem"] [:composer "Verdi"] [:composer "Bach"] [:country "Germany"]) ([:name "Requiem"] [:composer "Verdi"] [:composer "Mozart"] [:country "Austria"]) ([:name "Requiem"] [:composer "Verdi"] [:composer "Verdi"] [:country "Italy"]) ([:name "The art of the fugue"] [:composer "Bach"] [:composer "Bach"] [:country "Germany"]) ([:name "The art of the fugue"] [:composer "Bach"] [:composer "Mozart"] [:country "Austria"]) ([:name "The art of the fugue"] [:composer "Bach"] [:composer "Verdi"] [:country "Italy"]) ([:name "Musical offering"] [:composer "Bach"] [:composer "Bach"] [:country "Germany"]) ([:name "Musical offering"] [:composer "Bach"] [:composer "Mozart"] [:country "Austria"]) ([:name "Musical offering"] [:composer "Bach"] [:composer "Verdi"] [:country "Italy"]))

(s/join compositions composers)
;; => #{{:composer "Bach", :country "Germany", :name "Musical offering"} {:composer "Verdi", :country "Italy", :name "Requiem"} {:composer "Bach", :country "Germany", :name "The art of the fugue"} {:composer "Mozart", :country "Austria", :name "Requiem"}}
;; => 

(s/join composers nations {:country :nation})
;; => #{{:composer "Verdi", :country "Italy", :nation "Italy", :language "Italian"} {:composer "Bach", :country "Germany", :nation "Germany", :language "German"} {:composer "Mozart", :country "Austria", :nation "Austria", :language "German"}}

(s/project
 (s/join
  (s/select #(= (:name %) "Requiem") compositions)
  composers)
 [:country])
;; => #{{:country "Italy"} {:country "Austria"}}
