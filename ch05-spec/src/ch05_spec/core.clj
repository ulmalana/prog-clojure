(ns ch05-spec.core
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.test.check]
            [clojure.string :as str]
            [clojure.spec.gen.alpha :as gen])
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

;;; validating functions

;; seqs with structure
(s/def ::cat-example (s/cat :s string? :i int?))
;; => :ch05-spec.core/cat-example

(s/valid? ::cat-example ["test" 42])
;; => true

(s/conform ::cat-example ["test" 42])
;; => {:s "test", :i 42}

(s/def ::alt-example (s/alt :i int? :k keyword?))
;; => :ch05-spec.core/alt-example

(s/valid? ::alt-example [100])
;; => true

(s/valid? ::alt-example [:foo])
;; => true

(s/conform ::alt-example [42])
;; => [:i 42]

(s/conform ::alt-example [:bar 42])
;; => :clojure.spec.alpha/invalid

(s/conform ::alt-example [42 :bar])
;; => :clojure.spec.alpha/invalid

(s/explain ::alt-example [42 :bar])
;; (:bar) - failed: Extra input in: [1] spec: :ch05-spec.core/alt-example
;; => nil

;; repetition ops
(s/def ::oe (s/cat :odds (s/+ odd?) :even (s/? even?)))
;; => :ch05-spec.core/oe

(s/conform ::oe [1 3 5 100])
;; => {:odds [1 3 5], :even 100}

(s/conform ::oe [1 5 7])
;; => {:odds [1 5 7]}

(s/def ::odds (s/+ odd?))
;; => :ch05-spec.core/odds

(s/def ::optional-even (s/? even?))
;; => :ch05-spec.core/optional-even

(s/def ::oe2 (s/cat :odds ::odds :even ::optional-even))
;; => :ch05-spec.core/oe2

(s/conform ::oe2 [1 2 3 4])
;; => :clojure.spec.alpha/invalid

(s/conform ::oe2 [7 9 188])
;; => {:odds [7 9], :even 188}

;; variable arg list

;; spec println args (0 or more args)
(s/def ::println-args (s/* any?))
;; => :ch05-spec.core/println-args

;; spec intersection
(s/def ::intersection-args
  (s/cat :s1 set?
         :sets (s/* set?)))
;; => :ch05-spec.core/intersection-args

(s/conform ::intersection-args '[#{1 2} #{2 3} #{2 5}])
;; => {:s1 #{1 2}, :sets [#{3 2} #{2 5}]}

(s/def ::intersection-args-2 (s/+ set?))
;; => :ch05-spec.core/intersection-args-2

(s/conform ::intersection-args-2 '[#{1 2} #{2 3} #{2 5}])
;; => [#{1 2} #{3 2} #{2 5}]

(s/def ::meta map?)
;; => :ch05-spec.core/meta

(s/def ::validator ifn?)
;; => :ch05-spec.core/validator

(s/def ::atom-args
  (s/cat :x any? :options (s/keys* :opt-un [::meta ::validator])))
;; => :ch05-spec.core/atom-args

(s/conform ::atom-args [100 :meta {:foo 1} :validator int?])
;; => {:x 100,
;;     :options {:meta {:foo 1},
;;               :validator #function[clojure.core/int?]}}

;; multiarity args

;; optional first args
(s/def ::repeat-args
  (s/cat :n (s/? int?) :x any?))
;; => :ch05-spec.core/repeat-args

(s/conform ::repeat-args [100 "bar"])
;; => {:n 100, :x "bar"}

(s/conform ::repeat-args ["foo"])
;; => {:x "foo"}

;;; specifying functions
;; example: rand ([] [n])
(s/def ::rand-args (s/cat :n (s/? number?)))
;; => :ch05-spec.core/rand-args
(s/def ::rand-ret double?)
;; => :ch05-spec.core/rand-ret
(s/def ::rand-fn
  (fn [{:keys [args ret]}]
    (let [n (or (:n args) 1)]
      (cond (zero? n) (zero? ret)
            (pos? n) (and (>= ret 0) (< ret n))
            (neg? n) (and (<= ret 0) (> ret n))))))
;; => :ch05-spec.core/rand-fn

;; applying specs above to rand function
(s/fdef clojure.core/rand
  :args ::rand-args
  :ret ::rand-ret
  :fn ::rand-fn)
;; => clojure.core/rand

;; anonymous fn
(defn opposite [pred] ;; pred can be anonymous
  (comp not pred))
;; => #'ch05-spec.core/opposite

(s/def ::pred   ;; spec for anonymous fn
  (s/fspec :args (s/cat :x any?)
           :ret boolean?))
;; => :ch05-spec.core/pred

(s/fdef opposite
  :args (s/cat :pred ::pred) ;; apply ::pred spec to opposite
  :ret ::pred)
;; => ch05-spec.core/opposite


;; instrumenting functions
(stest/instrument 'clojure.core/rand)
;; => [clojure.core/rand]

(stest/instrument (stest/enumerate-namespace 'clojure.core))
;; => [clojure.core/rand]

(comment
  (rand :bar))
;; Execution error - invalid arguments to clojure.core/rand at (form-init5246894981458863557.clj:317).
;; :bar - failed: number? at: [:n] spec: :ch05-spec.core/rand-args

;; generative function test
(s/fdef clojure.core/symbol
  :args (s/cat :ns (s/? string?) :name string?)
  :ret symbol?
  :fn (fn [{:keys [args ret]}]
        (and (= (name ret) (:name args))
             (= (namespace ret) (:ns args)))))
;; => clojure.core/symbol

(stest/check 'clojure.core/symbol)
;; ({:spec
;;   #object[clojure.spec.alpha$fspec_impl$reify__2525 0x1a4e1c4e "clojure.spec.alpha$fspec_impl$reify__2525@1a4e1c4e"],
;;   :clojure.spec.test.check/ret
;;   {:result true,
;;    :pass? true,
;;    :num-tests 1000,
;;    :time-elapsed-ms 569,
;;    :seed 1673673045831},
;;   :sym clojure.core/symbol}

;; generating examples
(s/exercise (s/cat :ns (s/? string?) :name string?))
;; ([("") {:name ""}]
;;  [("") {:name ""}]
;;  [("") {:name ""}]
;;  [("17") {:name "17"}]
;;  [("1H") {:name "1H"}]
;;  [("lq12" "8") {:ns "lq12", :name "8"}]
;;  [("tG") {:name "tG"}]
;;  [("yQ" "vED6NxK") {:ns "yQ", :name "vED6NxK"}]
;;  [("65ns8") {:name "65ns8"}]
;;  [("7ZQ5ay" "r90HO") {:ns "7ZQ5ay", :name "r90HO"}])


;; combining generator
(defn big? [x]
  (> x 100))

(s/def ::big-odd (s/and odd? big?))
;; => :ch05-spec.core/big-odd

;; error  odd? works on more than one numeric type and
;; has no mapped generator. big? is custom pred and has no mappings as well.
(comment
  (s/exercise ::big-odd)
  ;; Execution error (ExceptionInfo) at ch05-spec.core/eval9660 (form-init7410786151270189036.clj:91).
  ;; Unable to construct gen at: [] for: odd?
  )

;; need to add initial pred that has mapped generator,
;; in this case int?
(s/def ::big-odd-int (s/and int? odd? big?))
;; => :ch05-spec.core/big-odd-int

(s/exercise ::big-odd-int)
;; ([6615 6615]
;;  [43843 43843]
;;  [437065 437065]
;;  [127 127]
;;  [275 275]
;;  [1099 1099]
;;  [1443 1443]
;;  [3773 3773]
;;  [273467 273467]
;;  [401 401])

;; custom generator
(s/def :marble/color-red
  (s/with-gen :marble/color #(s/gen #{:red})))
;; => :marble/color-red

(s/exercise :marble/color-red)
;; ([:red :red]
;;  [:red :red]
;;  [:red :red]
;;  [:red :red]
;;  [:red :red]
;;  [:red :red]
;;  [:red :red]
;;  [:red :red]
;;  [:red :red]
;;  [:red :red])

(s/def ::sku
  (s/with-gen (s/and string? #(str/starts-with? % "SKU-"))
    (fn [] (gen/fmap #(str "SKU-" %) (s/gen string?)))))
;; => :ch05-spec.core/sku

(s/exercise ::sku)
;; (["SKU-" "SKU-"]
;;  ["SKU-b" "SKU-b"]
;;  ["SKU-" "SKU-"]
;;  ["SKU-" "SKU-"]
;;  ["SKU-09" "SKU-09"]
;;  ["SKU-5" "SKU-5"]
;;  ["SKU-jM" "SKU-jM"]
;;  ["SKU-7W4" "SKU-7W4"]
;;  ["SKU-J3BTGY" "SKU-J3BTGY"]
;;  ["SKU-HNN" "SKU-HNN"])
