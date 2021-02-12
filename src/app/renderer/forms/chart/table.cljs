(ns app.renderer.forms.chart.table
  (:require [rum.core :as rum]
            [cljs-time.core :as t]
            [app.renderer.utils :refer [tc]]
            [citrus.core :as citrus]
            [app.renderer.forms.chart.left.task-list :as task-list]
            [app.renderer.forms.chart.right.stat-list :as stat-list]
            [app.renderer.forms.chart.middle.chart-list :as chart-list]
            [app.renderer.forms.chart.middle.timeline :as timeline]))

(def right-width 100)
(def h-header 30)
(def h-body 50)
(def left-min-width 200)
(def left-max-width 500)
(def init-width (- js/window.innerWidth right-width))
(def atom-width (atom (- js/window.innerWidth right-width)))
(def left-width (atom 200))
(def chart-ref (js/React.createRef))
(def left-ref (js/React.createRef))

(add-watch atom-width :window-width
           (fn [_ _ old new]
             (when (and (.-current chart-ref) @left-width)
               (set! (.-width (.-style (.-current chart-ref))) (str (- new @left-width) "px")))))

(add-watch left-width :resize-width
           (fn [_ _ old new]
             (set! (.-width (.-style (.-current chart-ref))) (str (- @atom-width new) "px"))
             (set! (.-width (.-style (.-current left-ref))) (str new "px"))))

(js/window.addEventListener "resize" #(reset! atom-width (- js/window.innerWidth right-width)))

(defn set-left-size [position]
  (when (and
          (< position left-max-width)
          (> position left-min-width))
    (reset! left-width position)))

(rum/defc middle < rum/reactive
                   {:key-fn (fn [_] "middle")}
  [r h-top]
  (citrus/dispatch! r :home :set-chart-ref chart-ref)
  (let [scale             (rum/react (citrus/subscription r [:home :scale]))
        chart-popper-open (rum/react (citrus/subscription r [:chart-popper :open]))
        wheel             (when (not chart-popper-open)
                            #(timeline/on-wheel-container % chart-ref scale))
        width             (str (* scale 1440) "px")]
    (tc {:component :box
         :opts      {:width     (str (- @atom-width @left-width) "px")
                     :onWheel   wheel
                     :ref       chart-ref
                     :className "middle"}
         :child     {:component :box
                     :opts      {:width width}
                     :child     (chart-list/ChartList r h-top h-header h-body)}})))

(rum/defc gap < rum/reactive
                {:key-fn (fn [_] "gap")}
  [r h-top]
  (tc {:component :draggable
       :opts      {:axis     "x"
                   :position {:x @left-width}
                   :onDrag   #(set-left-size (.-clientX %))}
       :child     {:component :box
                   :opts      {:className      "gap"
                               :height         (str "calc(100vh - " (+ h-top) "px)")
                               :display        "flex"
                               :flexDirection  "column"
                               :justifyContent "center"
                               :alignItems     "center"}
                   :child     {:component :more-vert
                               :opts      {:className "gap-dot"}}}}))

(rum/defc left < rum/reactive
                 {:key-fn (fn [_] "left")}
  [r h-top]
  (tc {:component :box
       :opts      {:width     (str @left-width "px")
                   :ref       left-ref
                   :className "left"
                   :key       "left"}
       :child     (task-list/TaskList r h-top h-header h-body)}))

(rum/defc right < rum/reactive
                  {:key-fn (fn [_] "right")}
  [r h-top]
  (tc {:component :box
       :opts      {:key       "right"
                   :flex      (str "0 0 " right-width "px")
                   :className "right"}
       :child     (stat-list/StatList r h-top h-header h-body)}))

(def load-mixin
  {:will-mount (fn [{[r] :rum/args :as state}]
                 (citrus/dispatch! r :chart :set-date (t/now))
                 (citrus/dispatch! r :chart :load-track-logs)
                 state)})

(rum/defc Table < rum/reactive
                  load-mixin
                  {:key-fn (fn [_] "table")}
  [r h-header]
  (tc {:component :box
       :opts      {:display "flex"}
       :child     [(left r h-header)
                   (gap r h-header)
                   (middle r h-header)
                   (right r h-header)]}))
