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


;; print list
(defmethod my-print-multi java.util.Collection [c]
  (.write *out* "(")
  (.write *out* (str/join " " c))
  (.write *out* ")"))

(my-println (take 6 (cycle [1 2 3])))
;; (1 2 3 1 2 3)
;; => nil

(my-println [1 2 3])
;; (1 2 3)
;; => nil

;; print vector
(defmethod my-print-multi clojure.lang.IPersistentVector [c]
  (.write *out* "[")
  (.write *out* (str/join " " c))
  (.write *out* "]"))

;; will throw exception since dispatch values match collection and IPersistentVector
(comment
  (my-println [1 2 3])
  ;; Execution error (IllegalArgumentException) at ch09-multimethod.core/my-println (core.clj:57).
  ;; Multiple methods in multimethod 'my-print-multi' match dispatch value: class clojure.lang.PersistentVector -> interface clojure.lang.IPersistentVector and interface java.util.Collection, and neither is preferred
  )

;; in case of such conflict,
;; tell clojure to prefer specific method
(prefer-method my-print-multi
               clojure.lang.IPersistentVector
               java.util.Collection)

(my-println (take 6 (cycle [1 2 3])))
;; (1 2 3 1 2 3)
;; => nil

(my-println [1 2 3])
;; [1 2 3]
;; => nil

;;; ad hoc taxonomy
(def test-savings {:id 1, :tag ::savings, ::balance 100M})
;; => #'ch09-multimethod.core/test-savings

(def test-checking {:id 2, :tag ::checking, ::balance 250M})
;; => #'ch09-multimethod.core/test-checking

(defmulti interest-rate :tag)
(defmethod interest-rate ::checking [_]
  0M)

(defmethod interest-rate ::savings [_]
  0.05M)

(interest-rate test-savings)
;; => 0.05M

(interest-rate test-checking)
;; => 0M

(defmulti account-level :tag)
(defmethod account-level ::checking [acct]
  (if (>= (:balance acct) 5000)
    ::premium
    ::basic))
(defmethod account-level ::savings [acct]
  (if (>= (:balance acct) 1000)
    ::premium
    ::basic))

(account-level {:id 1 :tag ::savings :balance 2000M})
;; => :ch09-multimethod.core/premium

(account-level {:id 1 :tag ::checking :balance 2000M})
;; => :ch09-multimethod.core/basic

;; bad approach
(comment
  (defmulti service-charge account-level)
  (defmethod service-charge ::basic [acct]
    (if (= (:tag acct) ::checking) 25 10))
  (defmethod service-charge ::premium [_] 0)
  )

;; better
(defmulti service-charge (fn [acct]
                           [(account-level acct) (:tag acct)]))

(defmethod service-charge [::basic ::checking] [_]
  25)

(defmethod service-charge [::basic ::savings] [_]
  20)

(defmethod service-charge [::premium ::checking] [_]
  0)

(defmethod service-charge [::premium ::savings] [_]
  0)

(service-charge {:tag ::checking :balance 1000})
;; => 25

(service-charge {:tag ::savings :balance 1000})
;; => 0

;; simplifying multimethod with inheritance
;; specify ::savings and ::checking as kind of ::account
(derive ::savings ::account)
(derive ::checking ::account)

(isa? ::savings ::account)
;; => true

;; simplified
(defmulti service-charge (fn [acct]
                           [(account-level acct) (:tag acct)]))

(defmethod service-charge [::basic ::checking] [_]
  25)

(defmethod service-charge [::basic ::savings] [_]
  20)

(defmethod service-charge [::premium ::account] [_]
  0)
