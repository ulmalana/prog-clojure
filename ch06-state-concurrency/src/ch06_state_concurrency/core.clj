(ns ch06-state-concurrency.core
  (:require [clojure.set :refer :all])
  (:import (java.awt Color Dimension)
           (javax.swing JPanel JFrame Timer JOptionPane)
           (java.awt.event ActionListener KeyListener))
  (:gen-class))

(defmacro import-static
  "Imports the named static fields and/or static methods of the class
  as (private) symbols in the current namespace.

  Example: 
      user=> (import-static java.lang.Math PI sqrt)
      nil
      user=> PI
      3.141592653589793
      user=> (sqrt 16)
      4.0

  Note: The class name must be fully qualified, even if it has already
  been imported.  Static methods are defined as MACROS, not
  first-class fns."
  [class & fields-and-methods]
  (let [only (set (map str fields-and-methods))
        the-class (. Class forName (str class))
        static? (fn [x]
                    (. java.lang.reflect.Modifier
                       (isStatic (. x (getModifiers)))))
        statics (fn [array]
                    (set (map (memfn getName)
                              (filter static? array))))
        all-fields (statics (. the-class (getFields)))
        all-methods (statics (. the-class (getMethods)))
        fields-to-do (intersection all-fields only)
        methods-to-do (intersection all-methods only)
        make-sym (fn [string]
                     (with-meta (symbol string) {:private true}))
        import-field (fn [name]
                         (list 'def (make-sym name)
                               (list '. class (symbol name))))
        import-method (fn [name]
                          (list 'defmacro (make-sym name)
                                '[& args]
                                (list 'list ''. (list 'quote class)
                                      (list 'apply 'list
                                            (list 'quote (symbol name))
                                            'args))))]
    `(do ~@(map import-field fields-to-do)
         ~@(map import-method methods-to-do))))

(import-static java.awt.event.KeyEvent VK_LEFT VK_RIGHT VK_UP VK_DOWN)

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

;;;;; agent
(def counter-agent (agent 0))
;; => #'ch06-state-concurrency.core/counter-agent

(send counter-agent inc)
;; => #agent[{:status :ready, :val 1} 0x385c25e1]

@counter-agent
;; => 1

;; validating agent and handling errors
(def counter-agent' (agent 0 :validator number?))
;; => #'ch06-state-concurrency.core/counter-agent'

(send counter-agent' (fn [_] "foo"))
;; => #agent[{:status :failed, :val 0} 0x1d410ff]

(agent-error counter-agent')
;; => #error {
 ;; :cause "Invalid reference state"
 ;; :via
 ;; [{:type java.lang.IllegalStateException
 ;;   :message "Invalid reference state"
 ;;   :at [clojure.lang.ARef validate "ARef.java" 33]}]
 ;; :trace
 ;; [[clojure.lang.ARef validate "ARef.java" 33]
 ;;  [clojure.lang.ARef validate "ARef.java" 46]
 ;;  [clojure.lang.Agent setState "Agent.java" 177]
 ;;  [clojure.lang.Agent$Action doRun "Agent.java" 115]
 ;;  [clojure.lang.Agent$Action run "Agent.java" 163]
 ;;  [java.util.concurrent.ThreadPoolExecutor runWorker "ThreadPoolExecutor.java" 1128]
 ;;  [java.util.concurrent.ThreadPoolExecutor$Worker run "ThreadPoolExecutor.java" 628]
 ;;  [java.lang.Thread run "Thread.java" 829]]}

(restart-agent counter-agent' 0)
;; => 0

@counter-agent'
;; => 0

(defn handler [agent err]
  (println "ERR!" (.getMessage err)))
;; => #'ch06-state-concurrency.core/handler

(def counter-agent-2
  (agent 0 :validator number? :error-handler handler))
;; => #'ch06-state-concurrency.core/counter-agent-2

(send counter-agent-2 (fn [_] "foo"))
;; ERR! Invalid reference state
;; => #agent[{:status :ready, :val 0} 0x7581a252]

(send counter-agent-2 inc)
;; => #agent[{:status :ready, :val 1} 0x7581a252]

@counter-agent-2
;; => 1

;; agent in transaction
(def backup-agent (agent "resources/messages-backup.clj"))
;; => #'ch06-state-concurrency.core/backup-agent

(defn add-message-with-backup [msg]
  (dosync
   (let [snapshot (commute messages conj msg)]
     (send-off backup-agent (fn [fname]
                              (spit fname snapshot)
                              fname))
     snapshot)))
;; => #'ch06-state-concurrency.core/add-message-with-backup

(add-message-with-backup (->Message "john" "message 1"))
;; => (#ch06_state_concurrency.core.Message{:sender "john", :text "message 1"} #ch06_state_concurrency.core.Message{:sender "john", :text "message 1"} #ch06_state_concurrency.core.Message{:sender "user 3", :text "test message"})

(add-message-with-backup (->Message "jane" "message 2"))
;; => (#ch06_state_concurrency.core.Message{:sender "jane", :text "message 2"} #ch06_state_concurrency.core.Message{:sender "john", :text "message 1"} #ch06_state_concurrency.core.Message{:sender "john", :text "message 1"} #ch06_state_concurrency.core.Message{:sender "user 3", :text "test message"})

;;; vars
(def ^:dynamic foo 42)
;; => #'ch06-state-concurrency.core/foo

foo
;; => 42

;; check var from other thread
(.start (Thread. (fn [] (println foo))))
;; 42
;; => nil

(binding [foo 10] foo)
;; => 10

;; binding vs let
(defn print-foo []
  (println foo))
;; => #'ch06-state-concurrency.core/print-foo

(let [foo "let foo"] (print-foo))
;; 42
;; => nil

(binding [foo "bound foo"] (print-foo))
;; bound foo
;; => nil

(defn ^:dynamic slow-double [n]
  (Thread/sleep 100)
  (* n 2))
;; => #'ch06-state-concurrency.core/slow-double

(defn calls-slow-double []
  (map slow-double [1 2 1 2 1 2]))
;; => #'ch06-state-concurrency.core/calls-slow-double

(time (dorun (calls-slow-double)))
;; "Elapsed time: 626.118715 msecs"
;; => nil

(defn demo-memoize []
  (time
   (dorun
    (binding [slow-double (memoize slow-double)]
      (calls-slow-double)))))
;; => #'ch06-state-concurrency.core/demo-memoize

(demo-memoize)
;; "Elapsed time: 207.806598 msecs"
;; => nil

;;;; snake game

;; functional part
(def width 75)
(def height 50)
(def point-size 10)
(def turn-millis 75)
(def win-length 5)
(def dirs {VK_LEFT [-1 0]
           VK_RIGHT [1 0]
           VK_UP [0 -1]
           VK_DOWN [0 1]})

(defn add-points [& pts]
  (vec (apply map + pts)))

(defn point-to-screen-rect [pt]
  (map #(* point-size %)
       [(pt 0) (pt 1) 1 1]))

(add-points [10 10] [-1 0])
;; => [9 10]

(point-to-screen-rect [5 10])
;; => (50 100 10 10)

(defn create-apple []
  {:location [(rand-int width) (rand-int height)]
   :color (Color. 210 50 90)
   :type :apple})

(defn create-snake []
  {:body (list [1 1])
   :dir [1 0]
   :type :snake
   :color (Color. 15 160 70)})

(defn move [{:keys [body dir] :as snake} & grow]
  (assoc snake :body (cons (add-points (first body) dir)
                           (if grow body (butlast body)))))

(move (create-snake))
;; => {:body ([2 1]), :dir [1 0], :type :snake, :color #object[java.awt.Color 0x687c49a3 "java.awt.Color[r=15,g=160,b=70]"]}

(move (create-snake) :grow)
;; => {:body ([2 1] [1 1]), :dir [1 0], :type :snake, :color #object[java.awt.Color 0x17b9c4e6 "java.awt.Color[r=15,g=160,b=70]"]}

(defn win? [{body :body}]
  (>= (count body) win-length))

(win? {:body [[1 1]]})
;; => false

(win? {:body [[1 1] [1 2] [1 3] [1 4] [1 5]]})
;; => true

(defn head-overlaps-body? [{[head & body] :body}]
  (contains? (set body) head))

(def lose? head-overlaps-body?)

(lose? {:body [[1 1] [1 2] [1 3]]})
;; => false

(lose? {:body [[1 1] [1 2] [1 1]]})
;; => true

(defn eats? [{[snake-head] :body} {apple :location}]
  (= snake-head apple))

(eats? {:body [[1 1] [1 2]]} {:location [2 2]})
;; => false

(eats? {:body [[2 2] [1 2]]} {:location [2 2]})
;; => true

(defn turn [snake newdir]
  (assoc snake :dir newdir))
;; => #'ch06-state-concurrency.core/turn

(turn (create-snake) [0 -1])
;; => {:body ([1 1]), :dir [0 -1], :type :snake, :color #object[java.awt.Color 0x28e3b3c4 "java.awt.Color[r=15,g=160,b=70]"]}

;; mutable part
(defn reset-game [snake apple]
  (dosync
   (ref-set apple (create-apple))
   (ref-set snake (create-snake)))
  nil)

(def test-snake (ref nil))
(def test-apple (ref nil))

(reset-game test-snake test-apple)
;; => nil
(deref test-snake)
;; => {:body ([1 1]), :dir [1 0], :type :snake, :color #object[java.awt.Color 0x1fe150a5 "java.awt.Color[r=15,g=160,b=70]"]}

@test-apple
;; => {:location [15 28], :color #object[java.awt.Color 0x4e436830 "java.awt.Color[r=210,g=50,b=90]"], :type :apple}

(defn update-direction [snake newdir]
  (when newdir
    (dosync
     (alter snake turn newdir))))

(update-direction test-snake [0 -1])
;; => {:body ([1 1]), :dir [0 -1], :type :snake, :color #object[java.awt.Color 0x1fe150a5 "java.awt.Color[r=15,g=160,b=70]"]}

(defn update-positions [snake apple]
  (dosync
   (if (eats? @snake @apple)
     (do (ref-set apple (create-apple))
         (alter snake move :grow))
     (alter snake move)))
  nil)

(reset-game test-snake test-apple)
;; => nil

(dosync
 (alter test-apple assoc :location [1 1]))
;; => {:location [1 1], :color #object[java.awt.Color 0x177a9d76 "java.awt.Color[r=210,g=50,b=90]"], :type :apple}

(update-positions test-snake test-apple)
;; => nil

(:body @test-snake)
;; => ([2 1] [1 1])

;;; snake gui
(defn fill-point [g pt color]
  (let [[x y width height] (point-to-screen-rect pt)]
    (.setColor g color)
    (.fillRect g x y width height)))

(defmulti paint
  (fn [g object & _]
    (:type object)))

(defmethod paint :apple
  [g {:keys [location color]}]
  (fill-point g location color))

(defmethod paint :snake
  [g {:keys [body color]}]
  (doseq [point body]
    (fill-point g point color)))

(defn game-panel [frame snake apple]
  (proxy [JPanel ActionListener KeyListener] []
    (paintComponent [g]
      (proxy-super paintComponent g)
      (paint g @snake)
      (paint g @apple))
    (actionPerformed [e]
      (update-positions snake apple)
      (when (lose? @snake)
        (reset-game snake apple)
        (JOptionPane/showMessageDialog frame "you lose!"))
      (when (win? @snake)
        (reset-game snake apple)
        (JOptionPane/showMessageDialog frame "you win"))
      (.repaint this))
    (keyPressed [e]
      (update-direction snake (dirs (.getKeyCode e))))
    (getPreferredSize []
      (Dimension. (* (inc width) point-size)
                  (* (inc height) point-size)))
    (keyReleased [e])
    (keyTyped [e])))

(defn game []
  (let [snake (ref (create-snake))
        apple (ref (create-apple))
        frame (JFrame. "Snake")
        panel (game-panel frame snake apple)
        timer (Timer. turn-millis panel)]
    (doto panel
      (.setFocusable true)
      (.addKeyListener panel))
    (doto frame
      (.add panel)
      (.pack)
      (.setVisible true))
    (.start timer)
    [snake, apple, timer]))
