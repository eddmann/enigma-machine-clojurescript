(ns enigma-machine.core
  (:require [clojure.set :refer [map-invert]]))

(def ^:private alphabet (seq "ABCDEFGHIJKLMNOPQRSTUVWXYZ"))

(def rotors
  {:I   {:out  (seq "EKMFLGDQVZNTOWYHXUSPAIBRCJ")
         :in   alphabet
         :step \Q},
   :II  {:out  (seq "AJDKSIRUXBLHWTMCQGZNPYFVOE")
         :in   alphabet
         :step \E}
   :III {:out  (seq "BDFHJLCPRTXVZNYEIWGAKMUSQO")
         :in   alphabet
         :step \V}})

(def reflectors
  {:A (zipmap alphabet (seq "EJMZALYXVBWFCRQUONTSPIKHGD"))
   :B (zipmap alphabet (seq "YRUHQSLDPXNGOKMIEBFZCWVJAT"))
   :C (zipmap alphabet (seq "FVPJIAOYEDRZXWGCTKUQSBNMHL"))})

(defn- step? [rotor]
  (= (:step rotor) (first (:in rotor))))

(defn- flip-rotor [rotor]
  {:in   (:out rotor),
   :out  (:in rotor),
   :step (:step rotor)})

(defn- rotate-rotor [rotor]
  (let [rotate #(concat (rest %) [(first %)])]
    (-> rotor
        (update :in rotate)
        (update :out rotate))))

(defn- rotate-rotors [[one two three]]
  (let [step-one? (step? one)
        step-two? (step? two)]
    [(rotate-rotor one)
     (if (or step-one? step-two?) (rotate-rotor two) two)
     ; double step
     (if step-two? (rotate-rotor three) three)]))

(defn- passthrough-rotors [rotors letter]
  (reduce
   (fn [letter rotor]
     (->> letter
          (get (zipmap alphabet (:out rotor)))
          (get (zipmap (:in rotor) alphabet))))
   letter
   rotors))

(defn- encode-letter [{:keys [rotors reflector plugboard]} letter]
  (let [plug           #(get plugboard % %),
        reflect        #(get reflector %)
        flipped-rotors (reverse (map flip-rotor rotors))]
    (->> letter
         plug
         (passthrough-rotors rotors)
         reflect
         (passthrough-rotors flipped-rotors)
         plug)))

(defn setup-plugboard [plugboard]
  (merge plugboard (map-invert plugboard)))

(defn encode-message [machine message]
  (let [machine (update machine :plugboard setup-plugboard)]
    (->> (seq message)
         (reduce
          (fn [machine letter]
            (let [machine (update machine :rotors rotate-rotors)]
              (update machine :cipher #(conj % (encode-letter machine letter)))))
          (merge machine {:cipher []}))
         :cipher
         (apply str))))
