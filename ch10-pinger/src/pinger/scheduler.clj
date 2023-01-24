(ns pinger.scheduler
  (:import [java.util.concurrent Executors ExecutorService
            ScheduledExecutorService
            ScheduledFuture TimeUnit]))

(set! *warn-on-reflection* true)

(defn scheduled-executor
  "create scheduled executor"
  ^ScheduledExecutorService [threads]
  (Executors/newScheduledThreadPool threads))

(defn periodically
  "schedule function f to run on executor e every 'delay'
  ms after a delay of 'initial-delay'.
  returns a ScheduledFuture"
  ^ScheduledFuture
  [^ScheduledExecutorService e f initial-delay delay]
  (.scheduleWithFixedDelay e f initial-delay delay TimeUnit/MILLISECONDS))

(defn shutdown-executor
  "shutdown an executor"
  [^ExecutorService e]
  (.shutdown e))
