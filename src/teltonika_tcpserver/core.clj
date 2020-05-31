(ns teltonika-tcpserver.core
  (:require
   [clojure.set :as set]
   [teltonika_tcpserver.conversion :as con]
   [teltonika_tcpserver.ioelement :as ioe]
   [teltonika-tcpserver.server :as TCP]
   [clojure.data.json :as json]
   [clojure.java.io :as io]))

(defn arities [f]
  (let [m (first (.getDeclaredMethods (class f)))
        p (.getParameterTypes m)]
    (alength p)))

(defn imei-length [input]
  (con/read-int input 2))

(defn accept-modem [output]
  (.write output (byte-array [0x00 0x01]) 0 2))

(defn imei? [input output]
  (let [response {:imei (con/read-text
                         input
                         (imei-length input))}]
    (accept-modem output)
    response))

(defn preamable [input]
  {:preamble (con/read-int input 4)})

(defn data-field-length [input]
  {:data-field-length (con/read-int input 4)})

(defn codec-id [input]
  {:codec-id (con/read-int input)})

(defn number-of-data-1 [input]
  {:number-of-data-1 (con/read-int input)})

(defn number-of-data-2 [input]
  {:number-of-data-2 (con/read-int input)})

(def preAvlDataFunctions [imei? preamable data-field-length codec-id number-of-data-1])

(defn to-coordinate [longitude-int] (/ (double longitude-int) 10000000))

(defn parse-avl-packet [stream]
  {:timestamp (con/read-int stream 8)
   :priority (con/read-int stream 1)
   :gps-element {:longitude (to-coordinate (con/read-int stream 4))
                 :latitude (to-coordinate (con/read-int stream 4))
                 :altitude (con/read-int stream 2)
                 :angle (con/read-int stream 2)
                 :satelites (con/read-int stream)
                 :speed (con/read-int stream 2)}
   :io-element (ioe/read-stream stream)})

(defn avl-data [stream packet-count]
  {:avl-data (into [] (for [x (range 0 packet-count)]
                        (parse-avl-packet stream)))})

(defn preAvlPacketData [inputStream outputStream]
  (into {} (for [x (range 0 (count preAvlDataFunctions))]
             (let [func (get preAvlDataFunctions x)]
               ;; this is a specific situation - here we're checking if the function needs both input and outputstream by inspecting it's argument count
               (if (= (arities func) 2)
                 (func inputStream outputStream)
                 (func inputStream))))))

(defn crc-16 [inputStream] {:crc-16 (con/read-int inputStream 4)})

(defn parse-packet [inputStream outputStream]
  (let [preAvlData (preAvlPacketData inputStream outputStream)]
    (conj
     preAvlData
     (avl-data inputStream (:number-of-data-1 preAvlData))
     (number-of-data-2 inputStream)
     (crc-16 inputStream)
     (.write outputStream (byte-array [0x00 0x00 0x00 (:number-of-data-1 preAvlData)]) 0 4))))

(defn handler [inputStream outputStream]
  (println (json/write-str (parse-packet inputStream outputStream))))

(def server
  (TCP/tcp-server
   :port    5002
   :handler (TCP/wrap-streams handler)))

(defn -main
  []
  (println "Starting a Teltonika Codec8/Codec8Extended TCP Server.")
  (TCP/start server))
