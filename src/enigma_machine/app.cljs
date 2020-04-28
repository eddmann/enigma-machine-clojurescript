(ns enigma-machine.app
  (:require [reagent.core :as r]
            [enigma-machine.machine :as machine]))

(defn- select-rotor [idx value]
  [:select
   {:key idx :on-change #(reset! value (.. % -target -value)) :value @value}
   (for [[key] machine/rotors]
     [:option {:key key :value key} key])])

(defn- select-reflector [value]
  [:div
   [:label "Reflector:"]
   [:select
    {:on-change #(reset! value (.. % -target -value)) :value @value}
    (for [[key] machine/reflectors]
      [:option {:key key :value key} key])]])

(defn- parse-upper-alpha [el]
  (-> el .-target .-value .toUpperCase (clojure.string/replace #"[^A-Z]" "")))

(defn- message-textarea [value]
  [:div
   [:textarea
    {:on-change #(reset! value (-> % parse-upper-alpha))
     :value     @value}]])

(defn- positions-input [value]
  [:div
   [:label "Positions:"]
   [:input
    {:type       "text"
     :max-length 3
     :value      @value
     :on-change  #(reset! value (-> % parse-upper-alpha))}]])

(defn- plugboard-input [value]
  [:div
   [:label "Plugboard:"]
   [:input
    {:type      "text"
     :value     @value
     :on-change #(reset! value
                         (-> % parse-upper-alpha (clojure.string/replace #"(.{2})" "$1 ") .trim))}]])

(defn- app []
  (let [rotors    [(r/atom "III") (r/atom "II") (r/atom "I")]
        reflector (r/atom "B")
        plugboard (r/atom "")
        positions (r/atom "AAA")
        message   (r/atom "")
        cipher    (r/atom "")
        encode    (fn []
                    (machine/encode-message
                     {:rotors    (map #(get machine/rotors (keyword @%)) rotors)
                      :positions @positions
                      :reflector (get machine/reflectors (keyword @reflector))
                      :plugboard (apply hash-map (seq (clojure.string/replace @plugboard " " "")))}
                     @message))]
    (fn []
      [:div
       [:h1 "Enigma Machine"]
       [:label "Rotors:"]
       (doall (map-indexed select-rotor rotors))
       (select-reflector reflector)
       (plugboard-input plugboard)
       (positions-input positions)
       (message-textarea message)
       [:button {:on-click #(reset! cipher (encode))} "Encode"]
       [:pre @cipher]])))

(defn ^:export init []
  (r/render [app] (.getElementById js/document "app")))
