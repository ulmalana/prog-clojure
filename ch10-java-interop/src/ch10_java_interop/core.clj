(ns ch10-java-interop.core
  (:import [java.io File FilenameFilter]
           [org.xml.sax InputSource]
           [org.xml.sax.helpers DefaultHandler]
           [java.io StringReader]
           [javax.xml.parsers SAXParserFactory])
  (:gen-class))

;;; creating java objects

;; direct use of java types
(defn say-hi []
  (println "hello from thread" (.getName (Thread/currentThread))))

(dotimes [_ 3]
  (.start (Thread. say-hi)))
;; hello from threadhello from thread Thread-16
;; hello from thread Thread-15
;; Thread-14
;; => nil

(java.util.Collections/binarySearch [1 13 42 100] 42)
;; => 2

;; implement java interfaces
(defn suffix-filter [suffix]
  (reify FilenameFilter
    (accept [this dir name]
      (.endsWith name suffix))))

(defn list-files [dir suffix]
  (seq (.list (File. dir) (suffix-filter suffix))))

(list-files "." ".clj")
;; => ("project.clj")

(defrecord Counter [n]
  Runnable
  (run [this] (println (range n))))
;; => ch10_java_interop.core.Counter

(def c (->Counter 5))

(.start (Thread. c))
;; (0 1 2 3 4)
;; => nil

(:n c)
;; => 5

(def c2 (assoc c :n 8))

(.start (Thread. c2))
;; (0 1 2 3 4 5 6 7)
;; => nil

;; extend classes with proxy

;; extend DefaultHandler class
(def print-element-handler
  (proxy [DefaultHandler] []
    (startElement [uri local qname atts]
      (println (format "saw element: %s" qname)))))

(defn demo-sax-parse [source handler]
  (.. SAXParserFactory newInstance newSAXParser
      (parse (InputSource. (StringReader. source)) handler)))

(demo-sax-parse
 "<foo><bar>Body of bar</bar></foo>"
 print-element-handler)
;; saw element: foo
;; saw element: bar
;; => nil

;; exception handling

;; rethrowing with ex-info
(comment
  (defn load-resource [path]
    (try
      (if (forbidden? path)
        (throw (ex-info "Forbidden resource"
                        {:status 403, :resource path}))
        (slurp path))
      (catch FileNotFoundException e
        (throw (ex-info "Missing resource"
                        {:status 404, :resource path})))
      (catch IOException e
        (throw (ex-info "Server error"
                        {:status 500, :resource path}))))))

;; responding to exception

;; not caller friendly
;; we just want true/false but it will throw exception
(defn class-available? [class-name]
  (Class/forName class-name))

(comment
  (class-available? "hehe.huhu")
  ;; Execution error (ClassNotFoundException) at java.net.URLClassLoader/findClass (URLClassLoader.java:476).
  ;; hehe.huhu
  )

;; better with handling
(defn class-available? [class-name]
  (try
    (Class/forName class-name) true
    (catch ClassNotFoundException _ false)))

(class-available? "java.lang.String")
;; => true

(class-available? "haha.hehe")
;; => false
