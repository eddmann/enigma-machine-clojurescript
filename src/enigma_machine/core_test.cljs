(ns enigma-machine.core-test
  (:require [cljs.test :refer (deftest is)]
            [clojure.test.check.clojure-test :refer (defspec)]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [enigma-machine.core :as core]))

(deftest message-without-plugboard
 (is
   (= "ILBDAAMTAZ"
      (core/encode-message
       {:rotors    [(:III core/rotors) (:II core/rotors) (:I core/rotors)]
        :reflector (:B core/reflectors)
        :plugboard {}}
       "HELLOWORLD"))))

(deftest message-with-plugboard
  (is
   (= "ILADBBMTBZ"
      (core/encode-message
       {:rotors    [(:III core/rotors) (:II core/rotors) (:I core/rotors)]
        :reflector (:B core/reflectors)
        :plugboard {\A \B}}
       "HELLOWORLD"))))

(deftest second-rotor-step
  (is
   (= "BDZGOWCXLTKSBTMCDLPBMUQOFXYHCXTGYJFLINHNXSHIUNTHEORXPQPKOVHCBUBTZSZSOOSTGOTFSODBBZZLXLCYZXIFGWFDZEEQIB"
      (core/encode-message
       {:rotors    [(:III core/rotors) (:II core/rotors) (:I core/rotors)]
        :reflector (:B core/reflectors)
        :plugboard {}}
       (apply str (repeat 102 "A"))))))

(def char-alpha
  (gen/fmap char (gen/choose 65 90)))

(def string-alpa
  (gen/fmap clojure.string/join (gen/vector char-alpha)))

(def gen-machine
  (gen/hash-map :rotors (-> (vals core/rotors) (gen/elements) (gen/vector 3))
                :reflector (-> (vals core/reflectors) (gen/elements))
                :plugboard (->> (gen/vector-distinct char-alpha)
                                (gen/such-that #(even? (count %)))
                                (gen/fmap #(apply hash-map %)))))

(defspec cipher-is-same-length-as-message
  (prop/for-all [machine gen-machine
                 message string-alpa]
    (let [cipher (core/encode-message machine message)]
      (= (count message) (count cipher)))))

(defspec cipher-message-encode-decode
  (prop/for-all [machine gen-machine
                 message string-alpa]
    (let [cipher (core/encode-message machine message)]
      (= message (core/encode-message machine cipher)))))
