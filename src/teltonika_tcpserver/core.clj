(ns teltonika-tcpserver.core)

(require '[clojure.set :as set])

(use 'teltonika-tcpserver.server)

(defn bytes->int [bytes]
  "Converts a byte array into an integer."
  (->> 
    bytes 
    (map (partial format "%02x")) 
    (apply (partial str "0x"))
    read-string))

(defn bytes->string [bytes]
  (apply str (map #(char (bit-and % 255)) bytes)))

(defn read-bytes 
  ([stream length]  
    (let 
      [buffer (byte-array length)]
      (.read stream buffer 0 length) 
        buffer))
  ([stream length bytes-to] 
    (bytes-to (read-bytes stream length))))


(defn read-string [stream length]
  (read-bytes stream length bytes->string))

(defn read-int [stream length]
  (read-bytes stream length bytes->int))

(defn imei-length [input] 
  (read-int input 2))

(defn imei [input] 
  {:imei (read-string input (imei-length input))})

(defn preamable [input]
  {:preamble (read-int input 4)})

(defn data-field-length [input] 
  {:data-field-length (read-int input 4)})

(defn codec-id [input] 
  {:codec-id (read-int input 1)})

(defn number-of-data-1 [input]
  {:number-of-data-1 (read-int input 1)})

(def preAvlDataFunctions [imei preamable data-field-length codec-id number-of-data-1])

(defn to-coordinate [longitude-int] (/ (double longitude-int) 10000000))

(defn avl-data [stream avl-length] 
  {:avl-data 
    {:timestamp (read-int stream 8)
     :priority (read-int stream 1)
     :gps-element {
       :longitude (to-coordinate (read-int stream 4))
       :latitude (to-coordinate (read-int stream 4))
       :altitude (read-int stream 2)
       :satelites (read-int stream 1)
       :speed (read-int stream 2)
     }
     :io-element (read-bytes stream (- avl-length 22))
    }})

(defn preAvlPacketData [inputStream outputStream] 
  (into {} (for [x (range 0 (count preAvlDataFunctions))] 
    ((get preAvlDataFunctions x) inputStream))))

(defn crc-16 [inputStream] {:crc-16 (read-int inputStream 4)})

(defn parse-packet [inputStream outputStream] 
  (let [preAvlData (preAvlPacketData inputStream outputStream)]
    (conj
      preAvlData 
      (avl-data inputStream (- (:data-field-length preAvlData) 3))
      (set/rename-keys (number-of-data-1 inputStream) {:number-of-data-1 :number-of-data-2})
      (crc-16 inputStream)
      ) 
  ))

(defn postAvlPacketData [inputStream outputStream]
  (println (parse-packet inputStream outputStream))
  )

(defn handler [inputStream outputStream]
 (println (postAvlPacketData inputStream outputStream)))



(def server
  (tcp-server 
    :port    5001
    :handler (wrap-streams handler)))

;(start server)
