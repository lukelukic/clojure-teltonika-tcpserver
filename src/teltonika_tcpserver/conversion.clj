(ns teltonika_tcpserver.conversion)

(defn bytes->int
  [bytes]
  "Converts a byte array into an integer."
  (->>
   bytes
   (map (partial format "%02x"))
   (apply (partial str "0x"))
   read-string))

(defn bytes->string
  [bytes]
  (apply
   str
   (map #(char (bit-and % 255)) bytes)))

(defn read-bytes
  ([stream length]
   (let [buffer (byte-array length)]
     (.read stream buffer 0 length)
     buffer))
  ([stream length bytes-to]
   (bytes-to (read-bytes stream length))))

(defn read-string
  [stream length]
  (read-bytes stream length bytes->string))

(defn read-int
  ([stream]
   (read-bytes stream 1 bytes->int))
  ([stream length]
   (read-bytes stream length bytes->int)))