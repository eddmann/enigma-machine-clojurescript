(ns enigma-machine.app
  (:require [reagent.core :as r]
            [enigma-machine.machine :as machine]))

(defn- select-rotor [idx value]
  [:label
   {:key idx}
   (apply str "Rotor " (inc idx) ":")
   [:select
    {:on-change #(reset! value (.. % -target -value)) :value @value}
    (for [[key] machine/rotors]
      [:option {:key key :value key} key])]])

(defn- select-reflector [value]
  [:label
   {:key "reflector"}
   "Reflector:"
   [:select
    {:on-change #(reset! value (.. % -target -value)) :value @value}
    (for [[key] machine/reflectors]
      [:option {:key key :value key} key])]])

(defn- parse-upper-alpha [el]
  (-> el .-target .-value .toUpperCase (clojure.string/replace #"[^A-Z]" "")))

(defn- message-textarea [value]
  [:label
   {:key "message"}
   "Message:"
   [:textarea
    {:on-change #(reset! value (-> % parse-upper-alpha))
     :value     @value}]])

(defn- positions-input [value]
  [:label
   {:key "positions"}
   "Positions:"
   [:input
    {:type       "text"
     :max-length 3
     :value      @value
     :on-change  #(reset! value (-> % parse-upper-alpha))}]])

(defn- plugboard-input [value]
  [:label
   {:key "plugboard"}
   "Plugboard:"
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
        message   (r/atom "HELLOWORLD")
        cipher    (r/atom "ILBDAAMTAZ")
        encode    (fn []
                    (machine/encode-message
                     {:rotors    (map #((keyword @%) machine/rotors) rotors)
                      :positions @positions
                      :reflector ((keyword @reflector) machine/reflectors)
                      :plugboard (apply hash-map (seq (clojure.string/replace @plugboard " " "")))}
                     @message))]
    (fn []
      [:div
       [:div
        {:class "columns"}
        (doall (map-indexed select-rotor rotors))]
       [:div
        {:class "columns"}
        (select-reflector reflector)
        (positions-input positions)
        (plugboard-input plugboard)]
       (message-textarea message)
       [:button {:on-click #(reset! cipher (encode))} "Encode"]
       [:pre @cipher]])))

(defn ^:export init []
  (reagent.dom/render [app] (.getElementById js/document "app")))
