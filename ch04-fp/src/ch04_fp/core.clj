(ns ch04-fp.core
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

;; bad
(defn stack-consuming-fibo [n]
  (cond
    (= n 0) 0
    (= n 1) 1
    :else (+ (stack-consuming-fibo (- n 1))
             (stack-consuming-fibo (- n 2)))))

(stack-consuming-fibo 42)
;; => 267914296

(comment
  (stack-consuming-fibo 1000000)
  ;; Execution error (StackOverflowError) at ch04-fp.core/stack-consuming-fibo (form-init7285338676474064515.clj:12).
  ;;null
  )

(defn tail-fibo [n]
  (letfn [(fib [current next n]
            (if (zero? n)
              current
              (fib next (+ current next) (dec n))))]
    (fib 0N 1N n)))

(tail-fibo 42)
;; => 267914296N

(comment
  (tail-fibo 1000000)
  ;; Execution error (StackOverflowError) at java.math.BigInteger/add (BigInteger.java:1309).
  ;; null
  )

(defn recur-fibo [n]
  (letfn [(fib [current next n]
            (if (zero? n)
              current
              (recur next (+ current next) (dec n))))]
    (fib 0N 1N n)))

(recur-fibo 42)
;; => 267914296N


(comment
  ;; can perform this
  (recur-fibo 1000000))

(defn lazy-seq-fibo
  ([]
   (concat [0 1] (lazy-seq-fibo 0N 1N)))
  ([a b]
   (let [n (+ a b)]
     (lazy-seq
      (cons n (lazy-seq-fibo b n))))))

(take 10 (lazy-seq-fibo))
;; => (0 1 1N 2N 3N 5N 8N 13N 21N 34N)

(rem (nth (lazy-seq-fibo) 1000000) 1000)
;; => 875N

(take 5 (iterate (fn [[a b]]
                   [b (+ a b)])
                 [0 1]))
;; => ([0 1] [1 1] [1 2] [2 3] [3 5])

(defn fibo []
  (map first (iterate (fn [[a b]]
                        [b (+ a b)])
                      [0N 1N])))

(def lots-fibs (take 1000000000 (fibo)))

(nth lots-fibs 100)
;; => 354224848179261915075N

(defn count-heads-pair [coll]
  (loop [cnt 0
         coll coll]
    (if (empty? coll)
      cnt
      (recur (if (= :h (first coll) (second coll))
               (inc cnt)
               cnt)
             (rest coll)))))

(count-heads-pair [:h :h :h :t :h])
;; => 2

(count-heads-pair [:h :t :h :t :h])
;; => 0

(defn by-pairs [coll]
  (let [take-pair (fn [c]
                    (when (next c) (take 2 c)))]
    (lazy-seq
     (when-let [pair (seq (take-pair coll))]
       (cons pair (by-pairs (rest coll)))))))

(by-pairs [:h :t :t :h :h :h])
;; => ((:h :t) (:t :t) (:t :h) (:h :h) (:h :h))

(defn count-heads-pair' [coll]
  (count (filter (fn [pair] (every? #(= :h %) pair))
                 (by-pairs coll))))

(partition 2 [:h :t :t :h :h :h])
;; => ((:h :t) (:t :h) (:h :h))

(partition 2 1 [:h :t :t :h :h :h])
;; => ((:h :t) (:t :t) (:t :h) (:h :h) (:h :h))

(def count-if (comp count filter))

(count-if odd? [1 2 3 4 5])
;; => 3

(defn count-runs [n pred coll]
  (count-if #(every? pred %) (partition n 1 coll)))

(count-runs 2 #(= % :h) [:h :t :t :h :h :h])
;; => 2

(count-runs 2 #(= % :t) [:h :t :t :h :h :h])
;; => 1

(count-runs 3 #(= % :h) [:h :t :t :h :h :h])
;; => 1

(def count-heads-pair'' (partial count-runs 2 #(= % :h)))

(defn faux-curry [& args]
  (apply partial partial args))

(def add-3 (partial + 3))
;; => #'ch04-fp.core/add-3

(add-3 7)
;; => 10

(def add-3' ((faux-curry +) 3))
(add-3' 7)
;; => 10

(declare my-odd? my-even?)

(defn my-odd? [n]
  (if (= n 0)
    false
    (my-even? (dec n))))

(defn my-even? [n]
  (if (= n 0)
    true
    (my-odd? (dec n))))

(map my-even? (range 10))
;; => (true false true false true false true false true false)

(map my-odd? (range 10))
;; => (false true false true false true false true false true)

(defn parity [n]
  (loop [n n
         par 0]
    (if (= n 0)
      par
      (recur (dec n) (- 1 par)))))

(map parity (range 10))
;; => (0 1 0 1 0 1 0 1 0 1)

(defn my-even?' [n]
  (= 0 (parity n)))

(defn my-odd?' [n]
  (= 1 (parity n)))

(defn trampoline-fibo [n]
  (let [fib (fn fib [f-2 f-1 current]
              (let [f (+ f-2 f-1)]
                (if (= n current)
                  f
                  #(fib f-1 f (inc current)))))]
    (cond
      (= n 0) 0
      (= n 1) 1
      :else (fib 0N 1 2))))

(trampoline trampoline-fibo 42)
;; => 267914296N

(rem (trampoline trampoline-fibo 1000000) 1000)
;; => 875N

(declare my-odd?'' my-even?'')

(defn my-odd?'' [n]
  (if (= n 0)
    false
    #(my-even?'' (dec n))))

(defn my-even?'' [n]
  (if (= n 0)
    true
    #(my-odd?'' (dec n))))

(trampoline my-even?'' 1000000)
;; => true


;; will blow stack
(declare replace-symbol replace-symbol-expr)

(defn replace-symbol [coll oldsym newsym]
  (if (empty? coll)
    ()
    (cons (replace-symbol-expr (first coll) oldsym newsym)
          (replace-symbol (rest coll) oldsym newsym))))

(defn replace-symbol-expr [symbol-expr oldsym newsym]
  (if (symbol? symbol-expr)
    (if (= symbol-expr oldsym)
      newsym
      symbol-expr)
    (replace-symbol symbol-expr oldsym newsym)))

(defn deeply-nested [n]
  (loop [n n
         result '(bottom)]
    (if (= n 0)
      result
      (recur (dec n) (list result)))))

(deeply-nested 5)
;; => ((((((bottom))))))

(deeply-nested 25)
;; => ((((((((((((((((((((((((((bottom))))))))))))))))))))))))))

(replace-symbol (deeply-nested 5) 'bottom 'deepest)
;; => ((((((deepest))))))

(comment
 (replace-symbol (deeply-nested 100000) 'bottom 'deepest)
 ;; Execution error (StackOverflowError) at ch04-fp.core/replace-symbol (form-init15987135216163020431.clj:223).
 ;; null
 )

(defn- coll-or-scalar [x & _]
  (if (coll? x)
    :collection
    :scalar))

(defmulti replace-symbol' coll-or-scalar)

(defmethod replace-symbol' :collection [coll oldsym newsym]
  (lazy-seq
   (when (seq coll)
     (cons (replace-symbol' (first coll) oldsym newsym)
           (replace-symbol' (rest coll) oldsym newsym)))))

(defmethod replace-symbol' :scalar [obj oldsym newsym]
  (if (= obj oldsym)
    newsym
    obj))

(set! *print-level* 25)

(replace-symbol' (deeply-nested 100000) 'bottom 'deepest)
;; => (((((((((((((((((((((((((#)))))))))))))))))))))))))

;;; male female sequences
(declare m f)
(defn m [n]
  (if (zero? n)
    0
    (- n (f (m (dec n))))))

(defn f [n]
  (if (zero? n)
    1
    (- n (m (f (dec n))))))

(comment
  (time (m 250))
  ;; "Elapsed time: 118363.538065 msecs"
  ;; => 155
  )

(def m (memoize m))
(def f (memoize f))

(comment
  (def m (memoize m))
  (def f (memoize f))
  )

(comment
  (time (m 250))
  ;; Execution error (StackOverflowError) at ch04-fp.core/m (core.clj:287).
  ;; null
  )
;;(time (m 250))

(def m-seq (map m (iterate inc 0)))
(def f-seq (map f (iterate inc 0)))

(nth m-seq 250)
;; => 155

(time (nth m-seq 10000))
;; Elapsed time: 34.617402 msecs
;; => 6180

;;; eager
(defn square [x]
  (* x x))

(defn sum-squares-seq [n]
  (vec (map square (range n))))

(defn sum-squares [n]
  (into [] (map square) (range n)))

(defn preds-seq []
  (->> (all-ns)
       (map ns-publics)
       (mapcat vals)
       (filter #(clojure.string/ends-with? % "?"))
       (map #(str (.-sym %)))
       vec))

(defn preds []
  (into []
        (comp (map ns-publics)
              (mapcat vals)
              (filter #(clojure.string/ends-with? % "?"))
              (map #(str (.-sym %))))
        (all-ns)))

(defn non-blank? [s]
  (not (clojure.string/blank? s)))

(defn non-blank-lines-seq [file-name]
  (let [reader (clojure.java.io/reader file-name)]
    (filter non-blank? (line-seq reader))))

(defn non-blank-lines [file-name]
  (with-open [reader (clojure.java.io/reader file-name)]
    (into [] (filter non-blank?) (line-seq reader))))

(defn non-blank-lines-eduction [reader]
  (eduction (filter non-blank?) (line-seq reader)))

(defn line-count [file-name]
  (with-open [reader (clojure.java.io/reader file-name)]
    (reduce
     (fn [cnt el] (inc cnt))
     0
     (non-blank-lines-eduction reader))))
