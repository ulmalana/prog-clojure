(ns ch05-spec.core
  (:require [clojure.spec.alpha :as s])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

;; spec definition
(s/def :my.app/company-name string?)
;; => :my.app/company-name

;; spec validation
(s/valid? :my.app/company-name "E-corp")
;; => true

(s/valid? :my.app/company-name 42)
;; => false

;; enumeration
(s/def :marble/color #{:red :green :blue})
;; => :marble/color

(s/valid? :marble/color :red)
;; => true

(s/valid? :marble/color :black)
;; => false

(s/def :bowling/roll #{0 1 2 3 4 5 6 7 8 9 10})
;; => :bowling/roll

(s/valid? :bowling/roll 8)
;; => true

;; range specs
(s/def :bowling/ranged-roll (s/int-in 0 11))
;; => :bowling/ranged-roll
(s/valid? :bowling/ranged-roll 11)
;; => false

;; handling nil
(s/def :my.app/company-name-2 (s/nilable string?))
;; => :my.app/company-name-2
(s/valid? :my.app/company-name-2 nil)
;; => true
(s/valid? :my.app/company-name-2 "test")
;; => true

;; instead of using set #{false, true, nil}, use this
(s/def ::nilable-boolean (s/nilable boolean?))
;; => :ch05-spec.core/nilable-boolean

(s/valid? ::nilable-boolean nil)
;; => true

;; logical specs for odd integer
(s/def ::odd-int (s/and int? odd?))
;; => :ch05-spec.core/odd-int

(s/valid? ::odd-int 7)
;; => true

(s/valid? ::odd-int 8)
;; => false

(s/valid? ::odd-int 3.14)
;; => false

(s/def ::odd-or-42 (s/or :odd ::odd-int :42 #{42}))
;; => :ch05-spec.core/odd-or-42

(s/valid? ::odd-or-42 33)
;; => true
(s/valid? ::odd-or-42 42)
;; => true

(s/conform ::odd-or-42 42)
;; => [:42 42]
(s/conform ::odd-or-42 9)
;; => [:odd 9]
(s/conform ::odd-or-42 22)
;; => :clojure.spec.alpha/invalid

(s/explain ::odd-or-42 22)
;; 22 - failed: odd? at: [:odd] spec: :ch05-spec.core/odd-int
;; 22 - failed: #{42} at: [:42] spec: :ch05-spec.core/odd-or-42
;; => nil

(s/explain-str ::odd-or-42 22)
;; => "22 - failed: odd? at: [:odd] spec: :ch05-spec.core/odd-int\n22 - failed: #{42} at: [:42] spec: :ch05-spec.core/odd-or-42\n"

;; collection specs
(s/def ::names (s/coll-of string?))

(s/valid? ::names ["Luffy" "Sanji"])
;; => true

(s/valid? ::names #{"Tanjiro" "Nezuko"})
;; => true

(s/valid? ::names '("Arisu"))
;; => true

(s/def ::my-set (s/coll-of int? :kind set? :min-count 2))
;; => :ch05-spec.core/my-set

(s/valid? ::my-set #{2 3})
;; => true
(s/valid? ::my-set #{4})
;; => false

(s/def ::scores (s/map-of string? int?))
;; => :ch05-spec.core/scores

(s/valid? ::scores {"A" 80 "B" 70})
;; => true

;; collection sampling: s/every for collections and s/every-kv
;; for maps. only check part of the collection (101 elements by default)

;; tuples
(s/def ::point (s/tuple float? float?))
;; => :ch05-spec.core/point
(s/conform ::point [1.1 2.2])
;; => [1.1 2.2]
(s/conform ::point [1.1 2.2 3.3])
;; => :clojure.spec.alpha/invalid

;; information maps
;;
;; example:
;;
;; {::music/id #uuid "40e30dc1-55ac-33e1-85d3-1f1508140bfc"
;;  ::music/artist "Rush"
;;  ::music/title "Moving Pictures"
;;  ::music/date #inst "1981-02-12"}
(s/def :music/id uuid?)
;; => :music/id
(s/def :music/artist string?)
;; => :music/artist
(s/def :music/title string?)
;; => :music/title
(s/def :music/date inst?)
;; => :music/date

;; for qualified keys
(s/def :music/release
  (s/keys :req [:music/id]
          :opt [:music/artist
                :music/title
                :music/date]))
;; => :music/release

;; for unqualified keys
(s/def :music/release-unqualified
  (s/keys :req-un [:music/id]
          :opt-un [:music/artist
                   :music/title
                   :music/date]))
;; => :music/release-unqualified
