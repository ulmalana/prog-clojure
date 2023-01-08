(ns ch01-get-started.core
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

;;;;; chapter 01
(defn blank? [str]
  (every? #(Character/isWhitespace %) str))

(blank? " test")
;; => false
(blank? "   ")
;; => true

(defrecord Person [first-name last-name])

(def foo (->Person "Riz" "Maulana"))

(:first-name foo)
;; => "Riz"

(defn hello-world [uname]
  (println (format "Halo, %" uname)))

(def accounts (ref #{}))
;; => #'ch01-get-started.core/accounts
(defrecord Account [id balance])

(dosync
 (alter accounts conj (->Account "Clojure" 1000.0)))

@accounts
;; => #{#ch01_get_started.core.Account{:id "Clojure", :balance 1000.0}}

(.. "hello" getClass getProtectionDomain)
;; => #object[java.security.ProtectionDomain 0x381af58b "ProtectionDomain  null\n null\n <no principals>\n java.security.Permissions@276b7039 (\n (\"java.security.AllPermission\" \"<all permissions>\" \"<all actions>\")\n)\n\n"]

(.start (new Thread (fn [] (println "Halo" (Thread/currentThread)))))
;; => nil

(def visitors (atom #{}))
(swap! visitors conj "Riz")
;; => #{"Riz"}

(deref visitors)
;; => #{"Riz"}

(defn hello [uname]
  (swap! visitors conj uname)
  (str "Halo, " uname))

(hello "rick")
;; => "Halo, rick"

@visitors;; => #{"rick" "Riz"}
