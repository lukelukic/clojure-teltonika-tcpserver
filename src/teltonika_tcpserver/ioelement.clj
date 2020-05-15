(ns teltonika_tcpserver.ioelement)

(require '[teltonika_tcpserver.conversion :as con])

(defn io-n-element [inputStream n-number]
    (let [elementsCount (con/read-int inputStream)
          returningMap []]
    
    (if 
        (= elementsCount 0) returningMap
            (into [] (for 
                [i (range 0 elementsCount)] 
                {:id (con/read-int inputStream)
                :value (con/read-int inputStream n-number)
                })
        ) 
    ))
)

(defn read [inputStream]
    (let [eventIoId (con/read-int inputStream)
          N (con/read-int inputStream)]
          
          {:eventIoId eventIoId
           :N N
           :N1 (io-n-element inputStream 1)
           :N2 (io-n-element inputStream 2)
           :N4 (io-n-element inputStream 4)
           :N8 (io-n-element inputStream 8)
          })
)


