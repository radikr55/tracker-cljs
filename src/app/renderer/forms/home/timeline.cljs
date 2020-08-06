(ns app.renderer.forms.home.timeline
  (:require [rum.core :as rum]
            ["@material-ui/core" :refer [Paper Box Tooltip Popper Fade]]
            ["@material-ui/core/styles" :refer [styled]]
            [citrus.core :as citrus]))

(def ref-container (rum/create-ref))

(def timeline-value ["01:00"  "02:00"  "03:00"  "04:00"  "05:00"  "06:00"  "07:00"  "08:00"  "09:00"  "10:00"  "11:00"  "12:00"  "13:00"  "14:00"  "15:00"  "16:00"  "17:00"  "18:00"  "19:00"  "20:00"  "21:00"  "22:00"  "23:00"])

(rum/defc time-cursor < rum/reactive
  [r]
  (let [show?          (rum/react (citrus/subscription r [:home :show-plus-line?]))
        mouse-position (rum/react (citrus/subscription r [:home :mouse-position]))
        format-time    (rum/react (citrus/subscription r [:home :mouse-time :format]))
        element        (.-current ref-container)]
    (when (and (boolean element) show?)
      (rum/adapt-class Box {:font-size "12px"
                            ;; :display   (when (not show?) "none")
                            :bgcolor   "secondary.main"
                            :position  "absolute"
                            :z-index   "99999"
                            :width     "30px"
                            :top       (+ (.-top (.getBoundingClientRect element)) 7)
                            :left      (- (:pageX mouse-position) 15)}
                       format-time))))

(rum/defc box < rum/reactive
  [r width content]
  (let [show? (not (rum/react (citrus/subscription r [:home :show-plus-line?])))]
    (rum/adapt-class Box {:flex            (str "0 0 " width  "px")
                          :height          "100%"
                          :display         "flex"
                          :overflow        "hidden"
                          :justify-content "center"
                          :align-items     "center"
                          :font-size       "12px"
                          :bgcolor         "secondary.main"}
                     (when show? content))))

(rum/defc subheader < rum/reactive
  [r category]
  (let [theme (rum/react (citrus/subscription r [:home :theme]))]))

(defn get-timeline [r]
  (map-indexed
    #(let [index %1
           value %2
           width 60]
       (cond
         (= index 0)                            (box r 90 value)
         (= (inc index) (count timeline-value)) (box r 90 value)
         :else                                  (box r width value)))
    timeline-value))

(rum/defc container < rum/reactive
  [r height]
  (rum/adapt-class Box
                   {:className    "timeline-container"
                    :onMouseEnter #(citrus/dispatch! r :home :plus-line true)
                    :onMouseLeave #(citrus/dispatch! r :home :plus-line false)
                    :ref          ref-container
                    :display      "flex"
                    :height       (str height "px")
                    :width        "100%"}
                   [(time-cursor r)
                    (get-timeline r)]))

(rum/defc Timeline < rum/reactive
  [r height]
  (container r height))
