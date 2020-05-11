(ns teltonika-tcpserver.core)

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

(defn avl-data [input avl-length] (read-bytes [input]))

(def preAvlDataFunctions [imei preamable data-field-length codec-id number-of-data-1])

(defn preAvlPacketData [inputStream outputStream] 
  (into {} (for [x (range 0 (count preAvlDataFunctions))] 
    ((get preAvlDataFunctions x) inputStream))))

(defn handler [inputStream outputStream]
  (println (preAvlPacketData inputStream outputStream)))


  ;(conj (imei inputStream) (preamable inputStream))

(def server
  (tcp-server 
    :port    5001
    :handler (wrap-streams handler)))

;(start server)
