(ns teltonika_tcpserver.codecmap)

(def element-definitions   [{:id 239 :name "Ignition"         :bytes 1 :type :unsigned :multiplier nil}
                            {:id 21  :name "GSM Signal"       :bytes 1 :type :unsigned :multiplier nil}
                            {:id 1   :name "Digital Input 1"  :bytes 1 :type :unsigned :multiplier nil}
                            {:id 66  :name "External Voltage" :bytes 2 :type :unsigned :multiplier 0.001}
                            {:id 241 :name "GSM Operator"     :bytes 4 :type :unsigned :multiplier nil}
                            {:id 78  :name "iButton"          :bytes 8 :type :unsigned :multiplier nil}])