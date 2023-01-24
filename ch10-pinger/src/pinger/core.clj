(ns pinger.core
  (:import [java.net URL HttpURLConnection])
  (:require [pinger.scheduler :as scheduler]))

(defn response-code [address]
  (let [conn ^HttpURLConnection (.openConnection (URL. address))
        code (.getResponseCode conn)]
    (when (< code 400)
      (-> conn .getInputStream .close))
    code))

(defn available? [address]
  (= 200 (response-code address)))

;; (defn -main []
;;   (let [addresses ["https://google.com"
;;                    "https://clojure.org"
;;                    "http://google.com/badurl"]]
;;     (while true
;;       (doseq [address addresses]
;;         (println address ":" (available? address)))
;;       (Thread/sleep (* 1000 60)))))

(defn check []
  (let [addresses ["https://google.com"
                   "https://clojure.org"
                   "http:google.com/badurl"]]
    (doseq [address addresses]
      (println address ":" (available? address)))))

(def immediately 0)
(def every-minute (* 60 1000))

(defn start [e]
  "REPL helper. start pinger on executor e"
  (scheduler/periodically e check immediately every-minute))

(defn stop [e]
  "REPL helper. stop executor e"
  (scheduler/shutdown-executor e))

(defn -main []
  (start (scheduler/scheduled-executor 1)))

(comment
  (require 'pinger.core :reload)
  ;; => nil
  (in-ns 'pinger.core)
  ;; => #object[clojure.lang.Namespace 0x52a36910 "pinger.core"]
  (response-code "https://google.com")
  ;; => 200
  (available? "https://google.com")
  ;; => true
  (available? "https://google.com/badurl")
  ;; => false

)

(comment
  clj -m pinger.core
  ;; WARNING: Implicit use of clojure.main with options is deprecated, use -M
  ;; https://google.com : true
  ;; https://clojure.org : true
  ;; http://google.com/badurl : false
)
