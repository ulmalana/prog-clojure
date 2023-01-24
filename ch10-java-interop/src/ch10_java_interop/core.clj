(ns ch10-java-interop.core
  (:import [java.io File FilenameFilter]
           [org.xml.sax InputSource]
           [org.xml.sax.helpers DefaultHandler]
           [java.io StringReader]
           [javax.xml.parsers SAXParserFactory])
  (:gen-class))

;;; creating java objects

;; direct use of java types
(defn say-hi []
  (println "hello from thread" (.getName (Thread/currentThread))))

(dotimes [_ 3]
  (.start (Thread. say-hi)))
;; hello from threadhello from thread Thread-16
;; hello from thread Thread-15
;; Thread-14
;; => nil

(java.util.Collections/binarySearch [1 13 42 100] 42)
;; => 2

;; implement java interfaces
(defn suffix-filter [suffix]
  (reify FilenameFilter
    (accept [this dir name]
      (.endsWith name suffix))))

(defn list-files [dir suffix]
  (seq (.list (File. dir) (suffix-filter suffix))))

(list-files "." ".clj")
;; => ("project.clj")

(defrecord Counter [n]
  Runnable
  (run [this] (println (range n))))
;; => ch10_java_interop.core.Counter

(def c (->Counter 5))

(.start (Thread. c))
;; (0 1 2 3 4)
;; => nil

(:n c)
;; => 5

(def c2 (assoc c :n 8))

(.start (Thread. c2))
;; (0 1 2 3 4 5 6 7)
;; => nil

;; extend classes with proxy

;; extend DefaultHandler class
(def print-element-handler
  (proxy [DefaultHandler] []
    (startElement [uri local qname atts]
      (println (format "saw element: %s" qname)))))

(defn demo-sax-parse [source handler]
  (.. SAXParserFactory newInstance newSAXParser
      (parse (InputSource. (StringReader. source)) handler)))

(demo-sax-parse
 "<foo><bar>Body of bar</bar></foo>"
 print-element-handler)
;; saw element: foo
;; saw element: bar
;; => nil

;; exception handling

;; rethrowing with ex-info
(comment
  (defn load-resource [path]
    (try
      (if (forbidden? path)
        (throw (ex-info "Forbidden resource"
                        {:status 403, :resource path}))
        (slurp path))
      (catch FileNotFoundException e
        (throw (ex-info "Missing resource"
                        {:status 404, :resource path})))
      (catch IOException e
        (throw (ex-info "Server error"
                        {:status 500, :resource path}))))))

;; responding to exception

;; not caller friendly
;; we just want true/false but it will throw exception
(defn class-available? [class-name]
  (Class/forName class-name))

(comment
  (class-available? "hehe.huhu")
  ;; Execution error (ClassNotFoundException) at java.net.URLClassLoader/findClass (URLClassLoader.java:476).
  ;; hehe.huhu
  )

;; better with handling
(defn class-available? [class-name]
  (try
    (Class/forName class-name) true
    (catch ClassNotFoundException _ false)))

(class-available? "java.lang.String")
;; => true

(class-available? "haha.hehe")
;; => false

;;; optimization

;; adding type hints
(defn describe-class [c]
  {:name (.getName c)
   :final (java.lang.reflect.Modifier/isFinal (.getModifiers c))})
;; => #'ch10-java-interop.core/describe-class

(set! *warn-on-reflection* true)

;; compiling describe-class will produce:
(comment
  ;; Reflection warning, /home/riz/prog-clojure/ch10-java-interop/src/ch10_java_interop/core.clj:122:10 - reference to field getName can't be resolved.
  ;; Reflection warning, /home/riz/prog-clojure/ch10-java-interop/src/ch10_java_interop/core.clj:123:47 - reference to field getModifiers can't be resolved.
  )

;; with type hints to remove warning
(defn describe-class [^Class c]
  {:name (.getName c)
   :final (java.lang.reflect.Modifier/isFinal (.getModifiers c))})
;; => #'ch10-java-interop.core/describe-class

(describe-class StringBuffer)
;; => {:name "java.lang.StringBuffer", :final true}

(comment
  (describe-class "bar")
  ;; Execution error (ClassCastException) at ch10-java-interop.core/describe-class (form-init17039568491775200357.clj:135).
  ;; class java.lang.String cannot be cast to class java.lang.Class (java.lang.String and java.lang.Class are in module java.base of loader 'bootstrap')
  )

(defn want-a-string [^String s]
  (println s))

(want-a-string "foo")
;; foo
;; => nil
(want-a-string 10)
;; 10
;; => nil

;; integer math
;; unchecked operators are fast but may overflow. use cautiosly.
(unchecked-add 9223372036854775807 1)
;; => -9223372036854775808

(comment
  (+ 9223372036854775807 1) ;; will overflow
  ;; Execution error (ArithmeticException) at ch10-java-interop.core/eval7863 (form-init17039568491775200357.clj:164).
  ;; integer overflow
  )

(+' 9223372036854775807 1)
;; => 9223372036854775808N

;; using primitives for performance

;; demo only
(defn sum-to [n]
  (loop [i 1
         sum 0]
    (if (<= i n)
      (recur (inc i) (+ i sum))
      sum)))

(sum-to 10)
;; => 55

(dotimes [_ 5] (time (sum-to 100000)))
;; "Elapsed time: 13.559515 msecs"
;; "Elapsed time: 2.756383 msecs"
;; "Elapsed time: 2.645531 msecs"
;; "Elapsed time: 2.694326 msecs"
;; "Elapsed time: 2.652738 msecs"
;; => nil

;; with type hints
(defn integer-sum-to ^long [^long n]
  (loop [i 1
         sum 0]
    (if (<= i n)
      (recur (inc i) (+ i sum))
      sum)))

(dotimes [_ 5] (time (integer-sum-to 100000)))
;; "Elapsed time: 9.370161 msecs"
;; "Elapsed time: 0.125435 msecs"
;; "Elapsed time: 0.198623 msecs"
;; "Elapsed time: 0.124353 msecs"
;; "Elapsed time: 0.178495 msecs"
;; => nil

(defn unchecked-sum-to ^long [^long n]
  (loop [i 1
         sum 0]
    (if (<= i n)
      (recur (inc i) (unchecked-add i sum))
      sum)))

;; unchecked operators are fast with possible data corruption
(dotimes [_ 5] (time (unchecked-sum-to 100000)))
"Elapsed time: 8.248158 msecs"
"Elapsed time: 0.078117 msecs"
"Elapsed time: 0.076303 msecs"
"Elapsed time: 0.074881 msecs"
"Elapsed time: 0.075452 msecs"
nil

(defn better-sum-to [n]
  (reduce + (range 1 (inc n))))

(defn best-sum-to [n]
  (/ (* n (inc n)) 2))

(dotimes [_ 5] (time (best-sum-to 100000)))
;; "Elapsed time: 0.068178 msecs"
;; "Elapsed time: 0.00491 msecs"
;; "Elapsed time: 0.004408 msecs"
;; "Elapsed time: 0.004629 msecs"
;; "Elapsed time: 0.004258 msecs"
;; => nil

;; java array
(make-array String 5)
;; => #object["[Ljava.lang.String;" 0x54417e3c "[Ljava.lang.String;@54417e3c"]

(seq (make-array String 5))
;; => (nil nil nil nil nil)

(defn painstakingly-create-array []
  (let [arr (make-array String 5)]
    (aset arr 0 "Painstaking")
    (aset arr 1 "to")
    (aset arr 2 "fill")
    (aset arr 3 "in")
    (aset arr 4 "arrays")
    arr))

(aget (painstakingly-create-array) 0)
;; => "Painstaking"

(alength (painstakingly-create-array))
;; => 5

(to-array ["easier" "array" "creation"])
;; => #object["[Ljava.lang.Object;" 0x1f05ed0e "[Ljava.lang.Object;@1f05ed0e"]

(def strings (into-array ["some" "strings" "here"]))

(seq (amap strings idx _ (.toUpperCase (aget strings idx))))
;; => ("SOME" "STRINGS" "HERE")

(areduce strings idx ret 0 (max ret (.length (aget strings idx))))
;; => 7
