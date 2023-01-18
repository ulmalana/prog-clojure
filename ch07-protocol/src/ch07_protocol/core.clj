(ns ch07-protocol.core
  (:import (java.io FileInputStream InputStreamReader BufferedReader
                    FileOutputStream OutputStreamWriter BufferedWriter)
           (java.security KeyStore KeyStore$SecretKeyEntry
                          KeyStore$PasswordProtection)
           (javax.crypto KeyGenerator Cipher
                         CipherInputStream CipherOutputStream)
           (javax.sound.midi MidiSystem))
  (:require [clojure.java.io :as io])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

;; our version of slurp
(defn gulp [src]
  (let [sb (StringBuilder.)]
    (with-open [reader (-> src
                           FileInputStream.
                           InputStreamReader.
                           BufferedReader.)]
      (loop [c (.read reader)]
        (if (neg? c)
          (str sb)
          (do
            (.append sb (char c))
            (recur (.read reader))))))))

;; our version of spit
(defn expectorate [dst content]
  (with-open [writer (-> dst
                         FileOutputStream.
                         OutputStreamWriter.
                         BufferedWriter.)]
    (.write writer (str content))))

(defn make-reader [src]
  (-> src FileInputStream. InputStreamReader. BufferedReader.))

(defn make-writer [dst]
  (-> dst FileOutputStream. OutputStreamWriter. BufferedWriter.))

;; refactor both gulp and expectorate
(defn gulp' [src]
  (let [sb (StringBuilder.)]
    (with-open [reader (make-reader src)]
      (loop [c (.read reader)]
        (if (neg? c)
          (str sb)
          (do
            (.append sb (char c))
            (recur (.read reader))))))))

(defn expectorate' [dst content]
  (with-open [writer (make-writer dst)]
    (.write writer (str content))))

;; using condp to support multiple types
(defn make-reader' [src]
  (-> (condp = (type src)
        java.io.InputStream src
        java.lang.String (FileInputStream. src)
        java.io.File (FileInputStream. src)
        java.net.Socket (.getInputStream src)
        java.net.URL (if (= "file" (.getProtocol src))
                       (-> src .getPath FileInputStream.)
                       (.openStream src)))
      InputStreamReader.
      BufferedReader.))

(defn make-writer' [dst]
  (-> (condp = (type dst)
        java.io.OutputStream dst
        java.io.File (FileOutputStream. dst)
        java.lang.String (FileOutputStream. dst)
        java.net.Socket (.getOutputStream dst)
        java.net.URL (if (= "file" (.getProtocol dst))
                       (-> dst .getPath FileOutputStream.)
                       (throw (IllegalArgumentException.
                               "Cant write to non-file URL"))))
      OutputStreamWriter.
      BufferedWriter.))

;;; java interface in clojure
(definterface IOFactory
  (^java.io.BufferedReader makeReader [this])
  (^java.io.BufferedWriter makeWriter [this]))

;;;; protocols
(defprotocol IOFactoryPro
  "A protocol for things that can be read from and written to"
  (make-reader-pro [this] "Creates a BufferedReader.")
  (make-writer-pro [this] "Creates a BufferedWriter."))

(defn gulp'' [src]
  (let [sb (StringBuilder.)]
    (with-open [reader (make-reader-pro src)]
      (loop [c (.read reader)]
        (if (neg? c)
          (str sb)
          (do
            (.append sb (char c))
            (recur (.read reader))))))))

(defn expectorate'' [dst content]
  (with-open [writer (make-writer-pro dst)]
    (.write writer (str content))))

(extend java.io.InputStream
  IOFactoryPro
  {:make-reader-pro (fn [src]
                      (-> src InputStreamReader. BufferedReader.))
   :make-writer-pro (fn [dst]
                      (throw (IllegalArgumentException.
                              "Cant open as an InputStream.")))})

(extend java.io.OutputStream
  IOFactoryPro
  {:make-reader-pro (fn [src]
                      (throw (IllegalArgumentException.
                              "Cant open as an OutputStream.")))
   :make-writer-pro (fn [dst]
                      (-> dst OutputStreamWriter. BufferedWriter.))})

(extend-type java.io.File
  IOFactoryPro
  (make-reader-pro [src]
    (make-reader-pro (FileInputStream. src)))
  (make-writer-pro [dst]
    (make-writer-pro (FileOutputStream. dst))))

(extend-protocol IOFactoryPro
  java.net.Socket
  (make-reader-pro [src]
    (make-reader-pro (.getInputStream src)))
  (make-writer-pro [dst]
    (make-writer-pro (.getOutputStream dst)))

  java.net.URL
  (make-reader-pro [src]
    (make-reader-pro (if (= "file" (.getProtocol src))
                       (-> src .getPath FileInputStream.)
                       (.openStream src))))
  (make-writer-pro [dst]
    (make-writer-pro (if (= "file" (.getProtocol dst))
                       (-> dst .getPath FileInputStream.)
                       (throw (IllegalArgumentException.
                               "Cant write to non-file URL"))))))

;;;;; datatypes
(deftype CryptoVault [filename keystore password])
;; => ch07_protocol.core.CryptoVault

(def vault (->CryptoVault "vault-file" "keystore" "toomanysecrets"))
;; => #'ch07-protocol.core/vault

(.filename vault)
;; => "vault-file"

(.keystore vault)
;; => "keystore"

(.password vault)
;; => "toomanysecrets"

(defprotocol Vault
  (init-vault [vault])
  (vault-output-stream [vault])
  (vault-input-stream [vault]))
;; => Vault

(defn vault-key [vault]
  (let [password (.toCharArray (.password vault))]
    (with-open [fis (FileInputStream. (.keystore vault))]
      (-> (doto (KeyStore/getInstance "JCEKS")
            (.load fis password))
          (.getKey "vault-key" password)))))

;; define datatype along with supported methods using protocol
(deftype CryptoVault [filename keystore password]
  Vault
  (init-vault [vault]
    (let [password (.toCharArray (.password vault))
          key (.generateKey (KeyGenerator/getInstance "AES"))
          keystore (doto (KeyStore/getInstance "JCEKS")
                     (.load nil password)
                     (.setEntry "vault-key"
                                (KeyStore$SecretKeyEntry. key)
                                (KeyStore$PasswordProtection. password)))]
      (with-open [fos (FileOutputStream. (.keystore vault))]
        (.store keystore fos password))))
  (vault-output-stream [vault]
    (let [cipher (doto (Cipher/getInstance "AES")
                   (.init Cipher/ENCRYPT_MODE (vault-key vault)))]
      (CipherOutputStream. (io/output-stream (.filename vault)) cipher)))
  (vault-input-stream [vault]
    (let [cipher (doto (Cipher/getInstance "AES")
                   (.init Cipher/DECRYPT_MODE (vault-key vault)))]
      (CipherInputStream. (io/input-stream (.filename vault)) cipher)))

  IOFactoryPro
  (make-reader-pro [vault]
    (make-reader-pro (vault-input-stream vault)))
  (make-writer-pro [vault]
    (make-writer-pro (vault-output-stream vault))))
;; => ch07_protocol.core.CryptoVault

(def vault (->CryptoVault "vault-file" "keystore" "toomanysecrets"))
;; => #'ch07-protocol.core/vault

(init-vault vault)
;; => nil

(expectorate'' vault "This is a test of CryptoVault by riz")
;; => nil

(gulp'' vault)
;; => "This is a test of CryptoVault by riz"

;;; supporting native spit and slurp for CryptoVault
(extend CryptoVault
  clojure.java.io/IOFactory
  (assoc clojure.java.io/default-streams-impl
         :make-input-stream (fn [x opts]
                              (vault-input-stream x))
         :make-output-stream (fn [x opts]
                               (vault-output-stream x))))
;; => nil

(spit vault "This is a test of CryptoVault using spit and slurp")
;; => nil

(slurp vault)
;; => "This is a test of CryptoVault using spit and slurp"

;;;;; records
;;; maps that can implement protocols, instead of only storing data
(defrecord Note [pitch octave duration])
;; => ch07_protocol.core.Note

;; example note: D# half note in the fourth octave
(->Note :D# 4 1/2)
;; => #ch07_protocol.core.Note{:pitch :D#, :octave 4, :duration 1/2}

;; access with their fields
(.pitch (->Note :D# 4 1/2))
;; => :D#

(map? (->Note :D# 4 1/2))
;; => true

;; access with keyword
(:octave (->Note :D# 4 1/2))
;; => 4

(assoc (->Note :D# 4 1/2) :pitch :Db :duration 1/4)
;; => #ch07_protocol.core.Note{:pitch :Db, :octave 4, :duration 1/4}

(update-in (->Note :D# 4 1/2) [:octave] inc)
;; => #ch07_protocol.core.Note{:pitch :D#, :octave 5, :duration 1/2}

;; addding optional field
(assoc (->Note :D# 4 1/2) :velocity 100)
;; => #ch07_protocol.core.Note{:pitch :D#, :octave 4, :duration 1/2, :velocity 100}

;; dissoc return new record if dissocing optional field
;; and return plain map if dissocing mandatory field
(dissoc (->Note :D# 4 1/2) :octave)
;; => {:pitch :D#, :duration 1/2}

(defprotocol MidiNote
  (to-msec [this tempo])
  (key-number [this])
  (play [this tempo midi-channel]))
;; => MidiNote

(extend-type Note
  MidiNote
  (to-msec [this tempo]
    (let [duration-to-bpm {1 240, 1/2 120, 1/4 60, 1/8 30, 1/16 15}]
      (* 1000
         (/ (duration-to-bpm (:duration this))
            tempo))))
  (key-number [this]
    (let [scale {:C 0, :C# 1, :Db 1, :D 2, :D# 3,
                 :Eb 3, :E 4, :F 5, :F# 6, :Gb 6,
                 :G 7, :G# 8, :Ab 8, :A 9, :A# 10,
                 :Bb 10, :B 11}]
      (+ (* 12 (inc (:octave this)))
         (scale (:pitch this)))))
  (play [this tempo midi-channel]
    (let [velocity (or (:velocity this) 64)]
      (.noteOn midi-channel (key-number this) velocity)
      (Thread/sleep (to-msec this tempo)))))
;; => nil

(defn perform [notes & {:keys [tempo] :or {tempo 120}}]
  (with-open [synth (doto (MidiSystem/getSynthesizer) .open)]
    (let [channel (aget (.getChannels synth) 0)]
      (doseq [note notes]
        (play note tempo channel)))))
;; => #'ch07-protocol.core/perform

(def close-encounters [(->Note :D 3 1/2)
                       (->Note :E 3 1/2)
                       (->Note :C 3 1/2)
                       (->Note :C 2 1/2)
                       (->Note :G 2 1/2)])
;; => #'ch07-protocol.core/close-encounters

;; play the notes
(perform close-encounters)
;; => nil

(def jaws (for [duration [1/2 1/2 1/4 1/4 1/8 1/8 1/8 1/8]
                pitch [:E :F]]
            (Note. pitch 2 duration)))
;; => #'ch07-protocol.core/jaws

(perform jaws)
;; => nil

(perform (map #(update-in % [:octave] inc) close-encounters))
;; => nil

(perform (map #(update-in % [:octave] dec) close-encounters))
;; => nil

(perform (for [velocity [64 80 90 100 110 120]]
           (assoc (Note. :D 3 1/2) :velocity velocity)))
;; => nil

;;; reify
;; for creating anonymous instance of datatype that implements protocol
(let [min-duration 250
      min-velocity 64
      rand-note (reify
                  ;; create anonymous instance that implements MidiNote protocol
                  MidiNote
                  (to-msec [this tempo] (+ (rand-int 1000) min-duration))
                  (key-number [this] (rand-int 100))
                  (play [this tempo midi-channel]
                    (let [velocity (+ (rand-int 100) min-velocity)]
                      (.noteOn midi-channel (key-number this) velocity)
                      (Thread/sleep (to-msec this tempo)))))]
  (perform (repeat 15 rand-note)))
;; => nil
