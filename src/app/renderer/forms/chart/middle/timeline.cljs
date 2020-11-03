(ns app.renderer.forms.chart.middle.timeline
  (:require [rum.core :as rum]
            [cljs-time.core :as t]
            [cljs-time.format :as ft]
            [app.renderer.utils :refer [tc]]
            [citrus.core :as citrus]))

(def mouse-event (atom nil))
(def f-time (atom ""))
(def show?       (atom false))
(def container-ref (js/React.createRef))
(def tooltip-ref (js/React.createRef))

(def timeline-value-1x ["" "01:00" "02:00" "03:00" "04:00" "05:00" "06:00" "07:00" "08:00" "09:00" "10:00" "11:00" "12:00" "13:00" "14:00" "15:00" "16:00" "17:00" "18:00" "19:00" "20:00" "21:00" "22:00" "23:00"])

(def timeline-value ["" ":30" "01:00" ":30" "02:00" ":30" "03:00" ":30" "04:00" ":30" "05:00" ":30" "06:00" ":30" "07:00" ":30" "08:00" ":30" "09:00" ":30" "10:00" ":30" "11:00" ":30" "12:00" ":30" "13:00" ":30" "14:00" ":30" "15:00" ":30" "16:00" ":30" "17:00" ":30" "18:00" ":30" "19:00" ":30" "20:00" ":30" "21:00" ":30" "22:00" ":30" "23:00" ":30" ""])

(defn format-time [time scale]
  (let [time    (/ time scale)
        minutes (t/minutes (rem time 60))
        hours   (t/hours (/ time 60))
        dtime   (t/plus (t/local-date-time (t/today)) minutes hours)]
    (ft/unparse (ft/formatter "HH:mm") dtime)))

(defn calc-time [event chart-ref scale]
  (when (and  (.-current  container-ref) (.-current  chart-ref))
    (let [element         (.-current  container-ref)
          scroll-position (.-scrollLeft (.-current  chart-ref))
          left            (.-left (.getBoundingClientRect element))
          time            (+ (- (:pageX event) (.-offsetLeft element)) scroll-position)
          format          (format-time time scale)]
      (reset! f-time format))))

(defn scroll-middle-box
  [posr ref scale]
  (let [current (.-current ref)]
    (when (not (nil? current))
      (let [scroll-position (.-scrollLeft current)
            pos             (+ posr scroll-position)]
        (set! (.-scrollLeft current) pos)
        (calc-time @mouse-event ref scale)))))

(defn on-mouse-move [e ref scale]
  (reset! mouse-event
          {:clientX (.-clientX e)
           :clientY (.-clientY e)
           :pageX   (.-pageX e)
           :pageY   (.-pageY e)})
  (calc-time @mouse-event ref scale))

(defn on-wheel-container [e ref scale]
  (let [delta (.-deltaY e)]
    (if (> delta 0)
      (scroll-middle-box -60 ref scale)
      (scroll-middle-box 60 ref scale))))

(rum/defc box < rum/reactive
  {:key-fn (fn [_ _ _ index] (str index))}
  [r width content index top?]
  [:div  {:style
          {:flex (str "0 0 " width  "px")}
          :className "timeline-box"}
   content])

(defn get-timeline [r]
  (let [scale           (rum/react (citrus/subscription r [:home :scale]))
        mock-scale      (if (= 1 scale) 2 scale)
        width           (str (* mock-scale 30))
        start-end-width (str (* mock-scale 15))]
    (map-indexed
      #(let [index %1
             value %2]
         (cond
           (= index 0)                            (box r start-end-width value index)
           (= (inc index) (count timeline-value)) (box r start-end-width value index)
           :else                                  (box r width value index)))
      (if (= scale 1) timeline-value-1x timeline-value))))

(rum/defc tooltip  < rum/reactive
  {:key-fn (fn [_ _ x] (str "tooltip" x))}
  [r height top?]
  (let [show        (rum/react show?)
        mouse-event (rum/react mouse-event)
        top         (if top? 25 (- 30))
        chart-ref   (rum/react (citrus/subscription r [:home :chart-ref]))
        format      (rum/react f-time)]
    (tc {:component :box
         :styl      {:position "absolute"
                     :z-index  1000
                     :top      (str (+ top (:clientY mouse-event)) "px")
                     :left     (str (:clientX mouse-event) "px")}
         :opts      {:display     (when (not show) "none")
                     :onMouseOver #(reset! show? false)
                     :ref         tooltip-ref}
         :child     {:component :paper
                     :child     format}})))

(rum/defc container < rum/reactive
  {:key-fn (fn [_] "timeline-container")}
  [r height top?]
  (let [chart-ref (rum/react (citrus/subscription r [:home :chart-ref]))
        scale     (rum/react (citrus/subscription r [:home :scale]))
        width     (str (* scale 1440) "px")]
    [:div {:style        {:width   width
                          :height  (str height "px")
                          :display "flex"}
           :class        (when (not top?) "bottom-scroll")
           :ref          container-ref
           :onMouseLeave #(reset! show? false)
           :onMouseOut   #(reset! show? false)
           :onMouseMove  #(on-mouse-move % chart-ref scale)
           :onMouseOver  #(reset! show? true)}
     [(tooltip r top?)
      (get-timeline r)]]))

(rum/defc activity < rum/reactive
  {:key-fn (fn [_ row index] (str index))}
  [r row index]
  (let [inactive? (:inactive-log row)
        scale     (rum/react (citrus/subscription r [:home :scale]))
        width     (str (* scale (:interval row)) "px")
        class     (cond
                    (nil? inactive?) "chart-activity-block-grey"
                    inactive?        "chart-activity-block-inactive"
                    :else            "chart-activity-block-active")]
    [:div {:style {:min-width width
                   :height    "100%"
                   :display   "flex"}
           :class class}]
    ))

(rum/defc activity-container < rum/reactive
  {:key-fn (fn [_] "acivity-container")}
  [r height top?]
  (let [scale         (rum/react (citrus/subscription r [:home :scale]))
        activity-list (rum/react (citrus/subscription r [:chart :activity]))
        width         (str (* scale 1440) "px")]
    [:div {:style {:width   width
                   :height  (str height "px")
                   :display "flex"}
           :class (if top? "bottom-border" "top-border")}
     (map-indexed #(activity r %2 %1) (first activity-list))]))

(rum/defc Timeline < rum/reactive
  [r height top?]
  (let [activity-height    5
        timeline-height    (- height activity-height)
        timeline-container (container r timeline-height top?)
        activity-container (activity-container r activity-height top?)]
    (if top?
      [timeline-container
       activity-container]
      [activity-container
       timeline-container])))
