(ns app.renderer.forms.home.timeline
  (:require [rum.core :as rum]
            ["@material-ui/core" :refer [Paper Box Tooltip Popper]]
            [cljs-time.core :as t]
            [cljs-time.format :as ft]
            [citrus.core :as citrus]))

(def mouse-time (atom 0))
(def show-tooltip (atom false))
(def mouse-position (atom {}))
(def ref-line   (rum/create-ref))

(def timeline-value [":30" "01:00" ":30" "02:00" ":30" "03:00" ":30" "04:00" ":30" "05:00" ":30" "06:00" ":30" "07:00" ":30" "08:00" ":30" "09:00" ":30" "10:00" ":30" "11:00" ":30" "12:00" ":30" "13:00" ":30" "14:00" ":30" "15:00" ":30" "16:00" ":30" "17:00" ":30" "18:00" ":30" "19:00" ":30" "20:00" ":30" "21:00" ":30" "22:00" ":30" "23:00" ":30"])

(rum/defc box < rum/reactive
  [r width content]
  (rum/adapt-class Box {:flex            (str "0 0 " width  "px")
                        :height          "100%"
                        :display         "flex"
                        :overflow        "hidden"
                        :justify-content "center"
                        :align-items     "center"
                        :font-size       "12px"
                        :bgcolor         "secondary.main"}
                   content))

(defn calc-time [event]
  (let [element (.-current  ref-line)
        padding 16
        left    (- padding (.-left (.getBoundingClientRect element)))]
    (reset! mouse-time (+ (- left (.-offsetLeft element)) (.-pageX event)))))

(defn format-time [time]
  (let [minutes (t/minutes (rem time 60))
        hours   (t/hours (/ time 60))
        dtime   (t/plus (t/local-date-time (t/today)) minutes hours)]
    (ft/unparse (ft/formatter "HH:mm") dtime)))

(defn propper-cursor )

(rum/defc tooltip < rum/reactive
  [r child]
  (let [mouse-event (rum/react mouse-position)
        mouseX      (:mouseX mouse-event)
        mouseY      (:mouseY mouse-event)]
    (rum/adapt-class Tooltip
                     {:PopperProps
                      {:anchorEl
                       {:getBoundingClientRect #(clj->js {:bottom mouseY
                                                          :right  mouseX
                                                          :width  0
                                                          :height 0})}}
                      :title (format-time (rum/react mouse-time))
                      :open  true}
                     child)))

(rum/defc container < rum/reactive
  [r]
  (tooltip r (rum/adapt-class Box
                              {:className    "timeline-container"
                               :display      "flex"
                               :height       "30px"
                               :ref          ref-line
                               :width        "100%"
                               :onMouseMove  (fn [e]
                                               (calc-time e)
                                               (reset! mouse-position {:mouseX (.-pageX e)
                                                                       :mouseY (.-pageY e)}))
                               :onMouseEnter #(reset! show-tooltip true)
                               :onMouseLeave #(reset! show-tooltip false)}
                              (map-indexed
                                #(let [index %1
                                       value %2
                                       width 30]
                                   (cond
                                     (= index 0)                            (box r 45 value)
                                     (= (inc index) (count timeline-value)) (box r 44 value)
                                     :else                                  (box r width value)))
                                timeline-value))))

(rum/defc Timeline < rum/reactive
  [r]
  (container r))
