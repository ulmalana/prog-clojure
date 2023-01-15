(ns ch06-state-concurrency.core
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

;;;; ref: coordinated, synchronous

(def current-track (ref "Audioslave: Gasoline"))
;; => #'ch06-state-concurrency.core/current-track

(deref current-track)
;; => "Audioslave: Gasoline"

@current-track
;; => "Audioslave: Gasoline"

;; change where a ref points to with ref-set
;; below error because we need to protect the update
(quote (ref-set current-track "Audioslave: Cochise")
  ;; Execution error (IllegalStateException) at ch06-state-concurrency.core/eval7799 (form-init4516949182439312335.clj:21).
  ;; No transaction running
  )

;; in clojure we can use transaction with dosync
;; to protect updates
(dosync
 (ref-set current-track "Audioslave: Cochise"))
;; => "Audioslave: Cochise"

@current-track
;; => "Audioslave: Cochise"

(def current-composer (ref "Audioslave"))
;; => #'ch06-state-concurrency.core/current-composer

(dosync
 (ref-set current-track "Numb")
 (ref-set current-composer "Linkin Park"))
;; => "Linkin Park"

@current-track
;; => "Numb"

@current-composer
;; => "Linkin Park"

;; alter
(defrecord Message [sender text])

(->Message "Riz" "Halo")
;; => #ch06_state_concurrency.core.Message{:sender "Riz", :text "Halo"}

(def messages (ref ()))
;; => #'ch06-state-concurrency.core/messages

(defn add-message [msg]
  (dosync (alter messages conj msg)))

(add-message (->Message "user 1" "bonjour"))
;; => (#ch06_state_concurrency.core.Message{:sender "user 1", :text "bonjour"})

(add-message (->Message "user 2" "guten tag"))
;; => (#ch06_state_concurrency.core.Message{:sender "user 2", :text "guten tag"}
;;     #ch06_state_concurrency.core.Message{:sender "user 1", :text "bonjour"})

(defn add-message-commute [msg]
  (dosync (commute messages conj msg)))

(def counter (ref 0))
;; => #'ch06-state-concurrency.core/counter

(defn next-counter []
  (dosync (alter counter inc)))

(next-counter)
;; => 1

(next-counter)
;; => 2

;; adding validation to refs
(defn valid-message? [msg]
  (and (:sender msg) (:text msg)))

(def validate-message-list
  #(every? valid-message? %))

(def messages
  (ref () :validator validate-message-list))

(comment
  (add-message "not a valid message")
  ;; Execution error (IllegalStateException) at ch06-state-concurrency.core/add-message (form-init4516949182439312335.clj:60).
  ;; Invalid reference state
  )

@messages
;; => ()

(add-message (->Message "user 3" "test message"))
;; => (#ch06_state_concurrency.core.Message{:sender "user 3", :text "test message"})

;;;; atom: uncoordinated, synchronous
(def current-track-atom (atom "Killing in the Name"))
;; => #'ch06-state-concurrency.core/current-track-atom

(deref current-track-atom)
;; => "Killing in the Name"

@current-track-atom
;; => "Killing in the Name"

(reset! current-track-atom "Know your enemy")
;; => "Know your enemy"

(def current-track-atom-map (atom {:title "Papercut" :composer "Zedd"}))
;; => #'ch06-state-concurrency.core/current-track-atom-map

(reset! current-track-atom-map
        {:title "Black" :composer "Pearl jam"})
;; => {:title "Black", :composer "Pearl jam"}

;; reset! changes the whole, swap! changes some part
(swap! current-track-atom-map assoc :title "Evenflow")
;; => {:title "Evenflow", :composer "Pearl jam"}
