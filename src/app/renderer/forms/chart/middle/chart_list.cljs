(ns app.renderer.forms.chart.middle.chart-list
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc]]
            [app.renderer.time-utils :as tu]
            [citrus.core :as citrus]
            [citrus.cursor :as c]
            [cljs-time.core :as t]
            [app.renderer.forms.chart.middle.timeline :as timeline]
            [app.renderer.forms.chart.middle.chart-popper :as popper]))

(def middle-list-ref (js/React.createRef))
(def last-active-time->ref (atom {:position nil}))

(defn open-dialog [r event row]
  (citrus/dispatch! r :chart-popper :open-popper
                    {:position {:mouseX (.-clientX event)
                                :mouseY (.-clientY event)}
                     :row      row}))

(defn check-position [not-nil? end-position]
  (when (and not-nil?
             (or (not (:time @last-active-time->ref))
                 (and (:time @last-active-time->ref)
                      (< end-position
                         (:position @last-active-time->ref)))))
    (reset! last-active-time->ref
            {:position end-position})))

(rum/defcs box < rum/reactive
                 (rum/local false ::isHighlight)
                 {:key-fn (fn [_ _ index] (str index))}
  [state r block index gray? row-code]
  (let [code         (:code block)
        state        (::isHighlight state)
        highlight?   (and (not code) @state)
        not-nil?     (boolean code)
        away?        (and (some? code) (empty? code))
        scale        (rum/react (citrus/subscription r [:home :scale]))
        current-task (rum/react (citrus/subscription r [:chart :current-task]))
        width        (str (* scale (:interval block)) "px")
        class        (cond-> "chart-block "
                             away? (str " chart-block-away ")
                             not-nil? (str " chart-block-blue ")
                             (and (not code) gray?) (str " chart-block-empty chart-block-gray ")
                             (and (not code) (not gray?)) (str " chart-block-empty chart-block-white ")
                             highlight? (str " chart-highlight ")
                             (empty? row-code) (str " chart-row-away ")
                             (seq row-code) (str " chart-row-blue ")
                             (= row-code current-task) (str " selected-row "))
        start        (:format-start block)
        end          (:format-end block)
        interval     (:format-interval block)
        title        (when not-nil? (str start " - " end "\n" interval))
        child        [{:component :typography
                       :opts      {:key       "title-interval"
                                   :className "chart-block-title"}
                       :child     (str start " - " end)}
                      {:component :typography
                       :opts      {:key "sum-interval"}
                       :styl      {:fontWeight "bold"}
                       :child     (str "(" interval ")")}]
        end-position (* scale (tu/get-interval (t/at-midnight (:end block)) (:end block)))]
    (check-position not-nil? end-position)
    (tc {:component :box
         :child     {:component :box
                     :opts      {:width       width
                                 :height      "100%"
                                 :onMouseOver #(reset! state true)
                                 :onMouseOut  #(reset! state false)
                                 :onClick     #(open-dialog r % (assoc block :code row-code))
                                 :display     "flex"
                                 :className   class
                                 :title       title}
                     :child     (when (and (not away?) not-nil?) child)}})))

(def scroll-mixin
  {:after-render (fn [{[r] :rum/args :as state}]
                   (when (and (:position @last-active-time->ref)
                              (-> @r :chart :auto-scroll)
                              (.-current middle-list-ref))
                     (let [parent-element   (.-parentNode (.-parentNode (.-current middle-list-ref)))
                           parent-width     (.-clientWidth parent-element)
                           last-active-left (:position @last-active-time->ref)
                           position         (- last-active-left (/ parent-width 2))]
                       (citrus/dispatch! r :chart :off-auto-scroll)
                       (.scrollTo parent-element
                                  (clj->js {:left position}))))
                   state)})

(rum/defc item < rum/reactive
                 {:key-fn (fn [_ row] (:code row))}
  [r row h-body index]
  (let [chart (rum/react (citrus/subscription r [:chart :chart]))
        scale (rum/react (citrus/subscription r [:home :scale]))
        code  (:code row)
        width (str (* scale 1440) "px")
        list  (->> chart
                   (filter #(= (:code %) code))
                   first
                   :chart)]
    (tc {:component :box
         :opts      {:height  (str h-body "px")
                     :display "flex"
                     :width   width}
         :child     (map-indexed #(box r %2 %1 (odd? index) code) list)})))

(rum/defc body < rum/reactive
                 scroll-mixin
                 {:key-fn (fn [_] "body")}
  [r h-top h-header h-body]
  (let [list (rum/react (citrus/subscription r [:chart :list]))]
    (citrus/dispatch! r :home :set-middle-list-ref middle-list-ref)
    (reset! last-active-time->ref {:position nil})
    (tc {:component :dnd-context
         :child     {:component :box
                     :opts      {:overflow "hidden"
                                 :ref      middle-list-ref
                                 :height   (str "calc(100vh - " (+ 2 h-top (* 2 h-header)) "px)")}
                     :child     (map-indexed #(item r %2 h-body %1) list)}})))

(rum/defc ChartList < rum/reactive
  [r h-top h-header h-body]
  (vector (timeline/Timeline r h-header true)
          (body r h-top h-header h-body)
          (popper/Popper r)
          (timeline/Timeline r h-header false)))
