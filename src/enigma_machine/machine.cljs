(ns enigma-machine.machine
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

(defn- invert-rotor [rotor]
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
     (if (or step-one? step-two?) (rotate-rotor two) two) ; double step
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
  (let [plug            #(get plugboard % %),
        reflect         #(get reflector %)
        inverted-rotors (reverse (map invert-rotor rotors))]
    (->> letter
         plug
         (passthrough-rotors rotors)
         reflect
         (passthrough-rotors inverted-rotors)
         plug)))

(defn- setup-plugboard [plugboard]
  (merge plugboard (map-invert plugboard)))

(defn- setup-rotors [rotors positions]
  (letfn
   [(setup-rotor [rotor position]
      (if (= (first (:in rotor)) position)
        rotor
        (recur (rotate-rotor rotor) position)))]
    (map setup-rotor rotors (seq positions))))

(defn- setup-machine [machine]
  (-> machine
      (update :plugboard setup-plugboard)
      (update :rotors setup-rotors (:positions machine "AAA"))
      (assoc :cipher [])))

(defn encode-message [machine message]
  (->> (seq message)
       (reduce
        (fn [machine letter]
          (let [machine (update machine :rotors rotate-rotors)]
            (update machine :cipher #(conj % (encode-letter machine letter)))))
        (setup-machine machine))
       :cipher
       (apply str)))
