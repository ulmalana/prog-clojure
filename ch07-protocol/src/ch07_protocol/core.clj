(ns ch07-protocol.core
  (:import (java.io FileInputStream InputStreamReader BufferedReader
                    FileOutputStream OutputStreamWriter BufferedWriter))
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
