(ns ch08-macro.core
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

;; writing control flow macro

;; bad example
(defn unless [expr form]
  (if expr nil form))

(unless false (println "this should print"))
;; this should print
;; => nil
(unless true (println "this should not print"))
;; this should not print
;; => nil

(defn unless [expr form]
  (println "About to test...")
  (if expr nil form))

;; println is evaluated first, before passing it to unless
(unless false (println "this should print"))
;; this should print
;; About to test...
;; => nil

;; println is evaluated first, before passing it to unless
(unless true (println "this should not print"))
;; this should not print
;; About to test...
;; => nil

(defmacro unless-macro [expr form]
  (list 'if expr nil form))
;; => #'ch08-macro.core/unless-macro

(unless-macro false (println "this should print"))
;; this should print
;; => nil

(unless-macro true (println "this should not print"))
;; => nil

;;;; macro expansion
(macroexpand-1 '(unless-macro false (println "this should print")))
;; => (if false nil (println "this should print"))

(defmacro bad-unless [expr form]
  (list 'if 'expr nil form))
;; => #'ch08-macro.core/bad-unless

(macroexpand-1 '(bad-unless false (println "this should print")))
;; => (if expr nil (println "this should print"))

(macroexpand '(.. arm getHand getFinger))
;; => (. (. arm getHand) getFinger)

(macroexpand '(and 1 2 3))
;; (let*
;;     [and__5531__auto__ 1]
;;   (if and__5531__auto__ (clojure.core/and 2 3) and__5531__auto_))

(macroexpand-1 '(when-not false (print "1") (print "2")))
;; => (if false nil (do (print "1") (print "2")))

;;; making macros simpler
;; chain is similar to .. macro
(defmacro chain
  ([x form] (list '. x form))
  ([x form & more] (concat (list 'chain (list '. x form)) more)))

(macroexpand '(chain arm getHand))
;; => (. arm getHand)

(macroexpand '(chain arm getHand getFinger))
;; => (. (. arm getHand) getFinger)

(defmacro chain [x form]
  `(. ~x ~form))

(macroexpand '(chain arm getHand))
;; => (. arm getHand)

;; not work well
(defmacro chain
  ([x form] `(. ~x ~form))
  ([x form & more] `(chain (. ~x ~form) ~more)))

(macroexpand '(chain arm getHand getFinger))
;; => (. (. arm getHand) (getFinger)) ;; getFinger is in a list

;; works better with splicing
(defmacro chain
  ([x form] `(. ~x ~form))
  ([x form & more] `(chain (. ~x ~form) ~@more)))

(macroexpand '(chain arm getHand getFinger))
;; => (. (. arm getHand) getFinger)

;;; introducing names in macros
;; bench is similar to time macro

;; wont work
(defmacro bench [expr]
  `(let [start (System/nanoTime)
         result ~expr]
     {:result result :elapsed (- (System/nanoTime) start)}))

(comment
  (bench (str "a" "b"))
  ;; Syntax error macroexpanding clojure.core/let at (*cider-repl prog-clojure/ch08-macro:localhost:41031(clj)*:47:18).
  ;; ch08-macro.core/start - failed: simple-symbol? at: [:bindings :form :local-symbol] spec: :clojure.core.specs.alpha/local-name
  ;; ch08-macro.core/start - failed: vector? at: [:bindings :form :seq-destructure] spec: :clojure.core.specs.alpha/seq-binding-form
  ;; ch08-macro.core/start - failed: map? at: [:bindings :form :map-destructure] spec: :clojure.core.specs.alpha/map-bindings
  ;; ch08-macro.core/start - failed: map? at: [:bindings :form :map-destructure] spec: :clojure.core.specs.alpha/map-special-binding
  )

(macroexpand-1 '(bench (str "a" "b")))
(comment ;; return value
  (clojure.core/let
      [ch08-macro.core/start (java.lang.System/nanoTime)
       ch08-macro.core/result (str "a" "b")]
    {:result ch08-macro.core/result,
     :elapsed (clojure.core/- (java.lang.System/nanoTime) ch08-macro.core/start)}))

;; correct bench
(defmacro bench [expr]
  `(let [start# (System/nanoTime)
         result# ~expr]
     {:result result# :elapsed (- (System/nanoTime) start#)}))

(bench (str "a" "b"))
;; => {:result "ab", :elapsed 69104}

(macroexpand-1 '(bench (str "a" "b")))
(comment
  (clojure.core/let
      [start__7898__auto__ (java.lang.System/nanoTime)
       result__7899__auto__ (str "a" "b")]
    {:result result__7899__auto__,
     :elapsed (clojure.core/- (java.lang.System/nanoTime) start__7898__auto__)}))

;;; taxonomy
(def slow-calc (delay (Thread/sleep 3000) "done!"))
;; => #'ch08-macro.core/slow-calc

(force slow-calc)
;; => "done!"

(force slow-calc)
;; => "done!"
