(ns teltonika_tcpserver.ioelement)

(require '[teltonika_tcpserver.conversion :as con]
         '[teltonika_tcpserver.codecmap :as cmap]
         )

(defn find-element [id] 
    (first (filter (fn [item] (= (:id item) id)) cmap/element-definitions))) 
    
    
(defn io-n-element [inputStream n-number]
    (let [elementsCount (con/read-int inputStream)
          returningMap []]
    (if 
        (= elementsCount 0) returningMap
            (into [] (for 
                [i (range 0 elementsCount)] 
                (let [ioElement {:id (con/read-int inputStream) :value (con/read-int inputStream n-number)}
                     ioElementDefinition (find-element (:id ioElement))]
                 
                 {
                     :id (:id ioElement)
                     :name (if (nil? (:name ioElementDefinition)) "Untracked" (:name ioElementDefinition))
                     :value (if (nil? (:multiplier ioElementDefinition))   
                        (:value ioElement)
                        (/ (:value ioElement) (:multiplier ioElementDefinition)))
                 })
                 )
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
          }))


