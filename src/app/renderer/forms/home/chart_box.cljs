(ns app.renderer.forms.home.chart-box
  (:require [rum.core :as rum]
            ["@material-ui/core" :refer [Paper Box IconButton Typography]]
            ["@material-ui/icons" :refer [AddCircleOutline]]
            [cljs-time.core :as t]
            [cljs-time.format :as ft]
            [app.renderer.forms.home.timeline :as tl]
            [app.renderer.forms.home.chart :as c]
            [app.renderer.forms.home.plus-line :as p]
            [citrus.core :as citrus]))

(def ref-container (rum/create-ref))
(def ref-chart (rum/create-ref))
(def mouse-time (atom 0))
(def show-tooltip (atom false))
(def mouse-position (atom {}))
(def chart-height 100)
(def timeline-height 30)
(def plus-line-height 50)

(defn to-box [child]
  (rum/adapt-class Box {:p 1} child))

(defn paper [child]
  (rum/adapt-class Paper {:elevation 3}
                   child))

(defn format-time [time]
  (let [minutes (t/minutes (rem time 60))
        hours   (t/hours (/ time 60))
        dtime   (t/plus (t/local-date-time (t/today)) minutes hours)]
    (ft/unparse (ft/formatter "HH:mm") dtime)))

(defn calc-time [event r]
  (let [element (.-current  ref-chart)
        padding 16
        left    (- padding (.-left (.getBoundingClientRect element)))
        time    (+ (- left (.-offsetLeft element)) (.-pageX event))
        format  (format-time time)]
    (citrus/dispatch! r :home :set-time {:format format
                                         :time   time})))

;; (defn position-x [mX]
;;   (let [end? (> (+ mX 70) (.-innerWidth js/window))]
;;     {:positionX (if end?
;;                   (- mX 70)
;;                   (- mX 24))
;;      :reverse?  end?}))

;; (rum/defc tooltip-button  < rum/reactive
;;   [hide? position]
;;   (let [element (.-current  ref-container)
;;         time    (format-time (rum/react mouse-time))
;;         child   [(rum/adapt-class     IconButton
;;                                       {:key   "icon-button"
;;                                        :title time}
;;                                       (rum/adapt-class   AddCircleOutline
;;                                                          {}))
;;                  (rum/adapt-class Typography {:display "inline"} time)]]
;;     [:div {:style {:position "absolute"
;;                    :title    "tasdf"
;;                    :display  (when hide? "none")
;;                    :top      (- (.-bottom (.getBoundingClientRect element)) 40)
;;                    :left     (:positionX position)}}
;;      (if (:reverse? position)
;;        (into [] (reverse child))
;;        child)]))

;; (rum/defc tooltip-line < rum/reactive
;;   [hide? mouse-event]
;;   (let [element (.-current  ref-container)]
;;     [:div {:style {:position         "absolute"
;;                    :z-index          "99999"
;;                    :display          (when hide? "none")
;;                    :width            "4px"
;;                    :height           (+ timeline-height chart-height)
;;                    :background-color "rgba(44, 113, 79, .5)"
;;                    :top              (.-top (.getBoundingClientRect element))
;;                    :left             (- (:pageX mouse-event) 2)}}]))

;; (rum/defc tooltip < rum/reactive
;;   [r]
;;   (let [element       (.-current  ref-container)
;;         mouse-event   (rum/react mouse-position)
;;         hide-tooltip? (not (rum/react show-tooltip))
;;         position      (position-x (:pageX mouse-event))]
;;     (when element
;;       [:div
;;        [(tooltip-button hide-tooltip? position)
;;         (tooltip-line hide-tooltip? mouse-event)
;;         ]])))

(rum/defc line-cursor < rum/reactive
  [r]
  [:div {:style {:position         "absolute"
                 :z-index          "99999"
                 :width            "10px"
                 :height           (+ timeline-height chart-height)
                 :background-color "black"}}])

(defn on-wheel-container [e r]
  (let [delta  (.-deltaY e)
        scroll (.-scrollLeft (.-current ref-container))]
    (if (> delta 0)
      (set! (.-scrollLeft (.-current ref-container)) (+ scroll 40))
      (set! (.-scrollLeft (.-current ref-container)) (- scroll 40)))
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
  (rum/adapt-class Box {:p 1}
                   (rum/adapt-class Box {:onWheel     #(on-wheel-container % r)
                                         :ref         ref-container
                                         :onMouseMove #(on-mouse-move % r)
                                         :overflow    "hidden"}
                                    [:div  {:ref ref-chart}
                                     [(c/Chart r chart-height)
                                      (tl/Timeline r timeline-height)
                                      (p/plus-line r plus-line-height)]])))

(rum/defc Chart-box < rum/reactive
  [r]
  (to-box (paper
            (container r))))
