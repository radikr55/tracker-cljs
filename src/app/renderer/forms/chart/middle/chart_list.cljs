(ns app.renderer.forms.chart.middle.chart-list
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc]]
            [citrus.core :as citrus]
            [app.renderer.forms.chart.middle.timeline :as timeline]
            [app.renderer.forms.chart.middle.chart-popper :as popper]))

(def middle-list-ref (js/React.createRef))

(defn open-dialog [r event row]
  (citrus/dispatch! r :chart-popper :open-popper
                    {:position {:mouseX (.-clientX event)
                                :mouseY (.-clientY event)}
                     :row      row}))

(rum/defc box < rum/reactive
  {:key-fn (fn [_ row index] (str index))}
  [r row index gray?]
  (let [not-nil? (not (nil? (:code row)))
        scale    (rum/react (citrus/subscription r [:home :scale]))
        width    (str (* scale (:interval row)) "px")
        class    (cond
                   not-nil? "chart-block-blue"
                   gray?    "chart-block-grey"
                   :else    "chart-block-white")
        start    (:format-start row)
        end      (:format-end row)
        interval (:format-interval row)
        title    (when not-nil? (str "start: " start "\nend: " end))
        child    [{:component :typography
                   :child     (str start " - " end)}
                  {:component :typography
                   :styl      {:fontWeight "bold"}
                   :child     (str "(" interval ")")}]]
    (tc {:component :box
         :opts      {:width     width
                     :height    "100%"
                     :onClick   (when not-nil? #(open-dialog r % row))
                     :display   "flex"
                     :className (str "chart-block " class)
                     :title     title}
         :child     (when not-nil? child)})))

(rum/defc item < rum/reactive
  {:key-fn (fn [_ row] (:code row))}
  [r row h-body index]
  (let [chart (rum/react (citrus/subscription r [:chart :chart]))
        scale (rum/react (citrus/subscription r [:home :scale]))
        width (str (* scale 1440) "px")
        list  (->> chart
                   (filter #(= (:code %) (:code row)))
                   first
                   :chart)]
    (tc {:component :box
         :opts      {:height  (str h-body "px")
                     :display "flex"
                     :width   width}
         :child     (map-indexed #(box r %2 %1 (odd? index)) list)})))

(rum/defc body  < rum/reactive
  [r h-top h-header h-body]
  (let [list      (rum/react (citrus/subscription r [:chart :list]))
        list-size (count list)]
    (citrus/dispatch! r :home :set-middle-list-ref middle-list-ref)
    (tc {:component :box
         :opts      {:overflow "hidden"
                     :ref      middle-list-ref
                     :height   (str "calc(100vh - " (+ 2 h-top (* 2 h-header)) "px)")}
         :child     (map-indexed #(item r %2 h-body %1) list)})))

(rum/defc ChartList < rum/reactive
  [r h-top h-header h-body]
  (vector (timeline/Timeline r h-header true)
          (body r h-top h-header h-body)
          (popper/Popper r)
          (timeline/Timeline r h-header false)))
