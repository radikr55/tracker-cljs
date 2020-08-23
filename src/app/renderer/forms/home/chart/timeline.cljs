(ns app.renderer.forms.home.chart.timeline
  (:require [rum.core :as rum]
            ["@material-ui/core" :refer [Paper Box Tooltip Popper Fade]]
            [citrus.core :as citrus]))


(def timeline-value ["" "01:00"  "02:00"  "03:00"  "04:00"  "05:00"  "06:00"  "07:00"  "08:00"  "09:00"  "10:00"  "11:00"  "12:00"  "13:00"  "14:00"  "15:00"  "16:00"  "17:00"  "18:00"  "19:00"  "20:00"  "21:00"  "22:00"  "23:00" ""])

(rum/defc time-cursor < rum/reactive
  [r]
  (when (rum/react (citrus/subscription r [:home :show-plus-line?]))
    (let [show?          (rum/react (citrus/subscription r [:home :show-plus-line?]))
          mouse-position (rum/react (citrus/subscription r [:home :mouse-position]))
          format-time    (rum/react (citrus/subscription r [:home :mouse-time :format]))
          container      (.-current (rum/react (citrus/subscription r [:home :ref-chart])))
          element        (.-current (rum/react (citrus/subscription r [:home :ref-timeline])))
          right-border   (.-right (.getBoundingClientRect container))
          left-border    (.-left (.getBoundingClientRect container))
          position       (- (:pageX mouse-position) 15)]
      (rum/adapt-class Box {:font-size "12px"
                            :bgcolor   "paper"
                            :position  "absolute"
                            :z-index   "99999"
                            :width     "30px"
                            :top       (+ (.-top (.getBoundingClientRect element)) 7)
                            :left      (when (and (> (+ position 10) left-border)
                                                  (< (- position 10) right-border)) position)}
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
                          :borderTop       1
                          :borderBottom    1
                          :bgcolor         "paper"}
                     (when show? content))))

(rum/defc subheader < rum/reactive
  [r category]
  (let [theme (rum/react (citrus/subscription r [:home :theme]))]))

(rum/defc get-timeline [r]
  (map-indexed
    #(let [index %1
           value %2
           width 60]
       (cond
         (= index 0)                            (box r 30 value)
         (= (inc index) (count timeline-value)) (box r 30 value)
         :else                                  (box r width value)))
    timeline-value))

(rum/defc container < rum/reactive
  [r height]
  (let [ref-timeline (rum/react (citrus/subscription r [:home :ref-timeline]))]
    (rum/adapt-class Box
                     {:className    "timeline-container"
                      :onMouseEnter #(citrus/dispatch! r :home :show-plus-line true)
                      :onMouseLeave #(citrus/dispatch! r :home :show-plus-line false)
                      :ref          ref-timeline
                      :display      "flex"
                      :height       (str height "px")
                      :width        "100%"}
                     [(rum/with-key (time-cursor r) "time-cursor")
                      (rum/with-key (get-timeline r) "timeline")])))

(rum/defc Timeline < rum/reactive
  [r height]
  (container r height))
