(ns ch09-multimethod.core
  (:require [clojure.string :as str])
  (:gen-class))

;;; without multimethod
(defn my-print [ob]
  (.write *out* ob))

(defn my-println [ob]
  (my-print ob)
  (.write *out* "\n"))

(my-println "hai")
;; hai
;; => nil

;; cant work with nil
(comment
  (my-println nil)
  ;; Execution error (NullPointerException) at java.io.PrintWriter/write (PrintWriter.java:524).
  ;; null
  )

;; use cond
(defn my-print [ob]
  (cond
    (nil? ob) (.write *out* "nil")
    (string? ob) (.write *out* ob)))

(my-println nil)
;; nil
;; => nil

(my-println [1 2 3]) ; print nothing
;;
;; => nil

(defn my-print-vector [ob]
  (.write *out* "[")
  (.write *out* (str/join " " ob))
  (.write *out* "]"))

(defn my-print [ob]
  (cond
    (vector? ob) (my-print-vector ob)
    (nil? ob) (.write *out* "nil")
    (string? ob) (.write *out* ob)))

(my-println [1 2 3])
;; [1 2 3]
;; => nil

;;; defining multimethod
(defmulti my-print-multi class)

(defn my-println [ob]
  (my-print-multi ob)
  (.write *out* "\n"))

(comment
  (my-println "bar")
  ;; Execution error (IllegalArgumentException) at ch09-multimethod.core/my-println (form-init5389489400414847758.clj:57).
  ;; No method in multimethod 'my-print-multi' for dispatch value: class java.lang.String
  )

(defmethod my-print-multi String [s]
  (.write *out* s))

(my-println "bar")
;; bar
;; => nil

(defmethod my-print-multi nil [s]
  (.write *out* "nil"))

(my-println nil)
;; nil
;; =>nil

;; inheritance aware
(defmethod my-print-multi Number [n]
  (.write *out* (.toString n)))

(my-println 42)
;; 42
;; => nil

(isa? Long Number)
;; => true

;; default implementation
(defmethod my-print-multi :default [s]
  (.write *out* "#<")
  (.write *out* (.toString s))
  (.write *out* ">"))

(my-println (java.sql.Date. 0))
;; #<1970-01-01>
;; =>nil

(my-println (java.util.Random.))
;; #<java.util.Random@591dc79e>
;; => nil

;; if :default already has other meaning
(defmulti my-print-multi class :default :everything-else)

(defmethod my-print-multi :everything-else [_]
  (.write *out* "Not implemented yet..."))
