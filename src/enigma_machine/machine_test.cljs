(ns enigma-machine.machine-test
  (:require [cljs.test :refer (deftest is)]
            [clojure.test.check.clojure-test :refer (defspec)]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [enigma-machine.machine :refer (rotors reflectors encode-message)]))

(deftest encode-message-with-empty-plugboard
  (is
   (= "ILBDAAMTAZ"
      (encode-message
       {:rotors    [(:III rotors) (:II rotors) (:I rotors)]
        :positions "AAA"
        :reflector (:B reflectors)
        :plugboard {}}
       "HELLOWORLD"))))

(deftest encode-message-with-plugboard
  (is
   (= "ILADBBMTBZ"
      (encode-message
       {:rotors    [(:III rotors) (:II rotors) (:I rotors)]
        :positions "AAA"
        :reflector (:B reflectors)
        :plugboard {\A \B}}
       "HELLOWORLD"))))

(deftest second-rotor-step
  (is
   (= "BDZGOWCXLTKSBTMCDLPBMUQOFXYHCXTGYJFLINHNXSHIUNTHEORXPQPKOVHCBUBTZSZSOOSTGOTFSODBBZZLXLCYZXIFGWFDZEEQIB"
      (encode-message
       {:rotors    [(:III rotors) (:II rotors) (:I rotors)]
        :positions "AAA"
        :reflector (:B reflectors)
        :plugboard {}}
       (apply str (repeat 102 "A"))))))

(def gen-char-upper-alpha
  (gen/fmap char (gen/choose 65 90)))

(def gen-string-upper-alpa
  (gen/fmap clojure.string/join (gen/vector gen-char-upper-alpha)))

(def gen-machine
  (gen/hash-map :rotors (-> (vals rotors) gen/elements (gen/vector 3))
                :positions (gen/fmap clojure.string/join (gen/vector gen-char-upper-alpha 3))
                :reflector (-> (vals reflectors) gen/elements)
                :plugboard (->> (gen/vector-distinct gen-char-upper-alpha {:min-elements 0 :max-elements 26})
                                (gen/fmap #(if (odd? (count %)) (rest %) %))
                                (gen/fmap #(apply hash-map %)))))

(defspec cipher-is-same-length-as-message
  (prop/for-all [machine gen-machine
                 message gen-string-upper-alpa]
    (let [cipher (encode-message machine message)]
      (= (count message) (count cipher)))))

(defspec encoded-cipher-matches-message
  (prop/for-all [machine gen-machine
                 message gen-string-upper-alpa]
    (let [cipher (encode-message machine message)]
      (= message (encode-message machine cipher)))))
