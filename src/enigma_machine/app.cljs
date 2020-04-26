(ns enigma-machine.app
  (:require [reagent.core :as r]
            [enigma-machine.core :as core]))

(defn select-rotor [idx value]
  [:select
   {:key idx :on-change #(reset! value (.. % -target -value)) :value @value}
   (for [[key] core/rotors]
     [:option {:key key :value key} key])])

(defn select-reflector [value]
  [:div
   [:label "Reflector:"]
   [:select
    {:on-change #(reset! value (.. % -target -value)) :value @value}
    (for [[key] core/reflectors]
      [:option {:key key :value key} key])]])

(defn message-textarea [value]
  [:div
   [:textarea
    {:on-change #(reset! value
                  (-> % .-target .-value .toUpperCase (clojure.string/replace #"[^A-Z]" "")))
     :value     @value}]])

(defn plugboard-input [value]
  [:div
   [:label "Plugboard:"]
   [:input
    {:type      "text"
     :value     @value
     :on-change #(reset! value
                  (-> % .-target .-value .toUpperCase (clojure.string/replace #"[^A-Z]" "") (clojure.string/replace #"(.{2})" "$1 ") .trim))}]])

(defn app []
  (let [rotors    [(r/atom "III") (r/atom "II") (r/atom "I")]
        reflector (r/atom "B")
        plugboard (r/atom "")
        message   (r/atom "")
        cipher    (r/atom "")
        encode    (fn []
                    (core/encode-message
                     {:rotors    (map #(get core/rotors (keyword @%)) rotors)
                      :reflector (get core/reflectors (keyword @reflector))
                      :plugboard (apply hash-map (seq (clojure.string/replace @plugboard " " "")))}
                     @message))]
    (fn []
      [:div
       [:h1 "Enigma Machine"]
       [:label "Rotors:"]
       (doall (map-indexed select-rotor rotors))
       (select-reflector reflector)
       (plugboard-input plugboard)
       (message-textarea message)
       [:button {:on-click #(reset! cipher (encode))} "Encode"]
       [:pre @cipher]])))

(defn ^:export init []
  (r/render [app] (.getElementById js/document "app")))
