(ns app.renderer.forms.home.chart-box
  (:require [rum.core :as rum]
            ["@material-ui/core" :refer [Paper Box IconButton Typography]]
            ["@material-ui/icons" :refer [AddCircleOutline]]
            [app.renderer.utils :refer [tc]]
            [cljs-time.core :as t]
            [cljs-time.format :as ft]
            [app.renderer.forms.home.chart.timeline :as tl]
            [app.renderer.forms.home.chart.chart :as c]
            [app.renderer.forms.home.chart.header :as header]
            [app.renderer.forms.home.chart.plus-line :as p]
            [citrus.core :as citrus]))

(def mouse-time (atom 0))
(def local-chart-ref (atom nil))
(def local-timeline-ref (atom nil))
(def show-tooltip (atom false))
(def mouse-position (atom {}))
(def chart-height 100)
(def timeline-height 30)
(def plus-line-height 50)

(defn to-box [child]
  (rum/adapt-class Box {:p 1} child))

(defn paper [child]
  (rum/adapt-class Paper {:elevation 4}
                   child))

(defn format-time [time]
  (let [minutes (t/minutes (rem time 60))
        hours   (t/hours (/ time 60))
        dtime   (t/plus (t/local-date-time (t/today)) minutes hours)]
    (ft/unparse (ft/formatter "HH:mm") dtime)))

(defn calc-time [event r]
  (let [element (.-current  @local-timeline-ref)
        padding 16
        left    (- padding (.-left (.getBoundingClientRect element)))
        time    (+ (- left (.-offsetLeft element)) (.-pageX event))
        format  (format-time time)]
    (citrus/dispatch! r :home :set-time {:format format
                                         :time   time})))

(rum/defc tooltip-line < rum/reactive
  [r]
  (when (rum/react (citrus/subscription r [:home :show-plus-line?]))
    (let [element      (.-current (rum/react (citrus/subscription r [:home :ref-chart])))
          mouse        (rum/react (citrus/subscription r [:home :mouse-position]))
          hide?        (rum/react (citrus/subscription r [:home :show-plus-line?]))
          right-border (.-right (.getBoundingClientRect element))
          left-border  (.-left (.getBoundingClientRect element))
          position     (- (:pageX mouse) 2)]
      [:div {:style {:position         "absolute"
                     :z-index          "99999"
                     :width            "4px"
                     :height           chart-height
                     :background-color "rgba(44, 113, 79, .5)"
                     :top              (.-top (.getBoundingClientRect element))
                     :left             (when (and (>  position left-border)
                                                  (< position right-border)) position)}}])))

(rum/defc line-cursor < rum/reactive
  [r]
  (let [element (.-current (rum/react (citrus/subscription r [:home :ref-chart])))]
    [:div {:style {:position         "absolute"
                   :z-index          "99999"
                   :width            "10px"
                   :top              (.-top (.getBoundingClientRect element))
                   :height           (+ timeline-height chart-height)
                   :background-color "black"}}]))

(defn on-wheel-container [e r]
  (let [delta  (.-deltaY e)
        scroll (.-scrollLeft (.-current @local-chart-ref))]
    (if (> delta 0)
      (set! (.-scrollLeft (.-current @local-chart-ref)) (+ scroll 40))
      (set! (.-scrollLeft (.-current @local-chart-ref)) (- scroll 40)))
    (calc-time e r)))

(defn on-mouse-move [e r]
  (calc-time e r)
  (citrus/dispatch! r :home :set-mouse-position
                    {:clientX (.-clientX e)
                     :clientY (.-clientY e)
                     :pageX   (.-pageX e)
                     :pageY   (.-pageY e)}))

(rum/defc container < rum/reactive
  [r]
  (let [ref-chart    (rum/react (citrus/subscription r [:home :ref-chart]))
        ref-timeline (rum/react (citrus/subscription r [:home :ref-timeline]))]
    (reset! local-chart-ref ref-chart)
    (reset! local-timeline-ref ref-timeline)
    (tc {:component :box
         :child     {:component :box
                     :opts      {:p         1
                                 :className "chart"}
                     :child     [(header/Header r)
                                 {:component :box
                                  :opts      {:onWheel     #(on-wheel-container % r)
                                              :ref         ref-chart
                                              :onMouseMove #(on-mouse-move % r)
                                              :overflow    "hidden"}
                                  :child     [(c/Chart r chart-height)
                                              (tooltip-line r)
                                              (tl/Timeline r timeline-height)
                                              (p/plus-line r plus-line-height)]}]}})))

(rum/defc Chart-box < rum/reactive
  [r]
  (to-box (paper
            (container r))))
