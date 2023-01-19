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
