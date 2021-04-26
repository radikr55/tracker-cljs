(ns app.renderer.forms.chart.middle.chart-list
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc]]
            [app.renderer.time-utils :as tu]
            [citrus.core :as citrus]
            [citrus.cursor :as c]
            [cljs-time.core :as t]
            [app.renderer.forms.chart.middle.timeline :as timeline]
            ["react-dnd" :refer [useDrag useDrop]]
            [app.renderer.forms.chart.middle.chart-popper :as popper]))

(def middle-list-ref (js/React.createRef))
(def last-active-time->ref (atom {:position nil}))

(defn open-dialog [r event row]
  (citrus/dispatch! r :chart-popper :open-popper
                    {:position {:mouseX (.-clientX event)
                                :mouseY (.-clientY event)}
                     :row      row}))

(defn check-position [show? end-position]
  (when (and show?
             (or (not (:time @last-active-time->ref))
                 (and (:time @last-active-time->ref)
                      (< end-position
                         (:position @last-active-time->ref)))))
    (reset! last-active-time->ref
            {:position end-position})))

(rum/defc box < rum/static
  [r block index gray? row-code scale current-task drag props line-props]
  (let [{dragging? :dragging?} props
        code         (:code block)
        not-nil?     (boolean code)
        away?        (and (some? code) (empty? code))
        width        (str (* scale (:interval block)) "px")
        place-class  (cond-> "chart-block "
                             gray? (str " chart-block-empty chart-block-gray ")
                             (not gray?) (str " chart-block-empty chart-block-white ")
                             (empty? row-code) (str " chart-row-away ")
                             (seq row-code) (str " chart-row-blue ")
                             (= row-code current-task) (str " selected-row ")
                             (:over? line-props) (str " chart-active-drop-area "))
        class        (cond-> "chart-block "
                             away? (str " chart-block-away ")
                             not-nil? (str " chart-block-blue ")
                             (and (not code) gray?) (str " chart-block-empty chart-block-gray ")
                             (and (not code) (not gray?)) (str " chart-block-empty chart-block-white ")
                             (empty? row-code) (str " chart-row-away ")
                             (seq row-code) (str " chart-row-blue ")
                             (:stub? block) (str " chart-block-stub ")
                             (= row-code current-task) (str " selected-row ")
                             (:over? line-props) (str " chart-active-drop-area "))
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
        end-position (* scale (tu/get-interval (t/at-midnight (:end block)) (:end block)))
        on-click     (if (not code)
                       (when (not (:stub? block))
                         #(open-dialog r % (assoc block :code row-code
                                                        :min-start (:start block)
                                                        :max-end (:end block))))
                       #(open-dialog r % (assoc block :code row-code
                                                      :max-start (:start block)
                                                      :min-end (:end block))))]
    (check-position (and not-nil? (not= (:interval block) 0)) end-position)
    (tc (if dragging?
          {:component :box
           :opts      {:height    "100%"
                       :width     width
                       :display   "flex"
                       :className place-class}}
          {:component :box
           :opts      {:title     (when (not dragging?) title)
                       :width     width
                       :ref       (when not-nil? drag)
                       :height    "100%"
                       :onClick   on-click
                       :display   "flex"
                       :className class}
           :child     (when (and (not away?) not-nil?) child)}))))

(rum/defc wrap-box < rum/static
  [r block index gray? row-code scale current-task line-props]
  (let [[props drag drop] (useDrag (clj->js {:item    {:type  "box"
                                                       :block block}
                                             :collect (fn [monitor] {:dragging? (.isDragging monitor)})}))]
    (box r block index gray? row-code scale current-task drag props line-props)))

(rum/defc item < rum/static
  [r row h-body index chart scale current-task]
  (let [code    (:code row)
        width   (str (* scale 1439) "px")
        list    (->> chart
                     (filter #(= (:code %) code))
                     first
                     :chart)
        on-drop (fn [r item drop-code]
                  (let [{start :start
                         end   :end
                         code  :code} (:block (js->clj item :keywordize-keys true))]
                    (if (not= code drop-code) (citrus/dispatch! r :chart-popper :save-time
                                                                start end drop-code)))
                  (clj->js {}))
        collect (fn [monitor] {:over? (and (.isOver monitor)
                                           (not= code
                                                 (-> (.getItem monitor)
                                                     (js->clj :keywordize-keys true)
                                                     :block
                                                     :code)))})
        [prop drop] (useDrop (clj->js {:accept  "box"
                                       :drop    #(on-drop r %1 code)
                                       :collect collect}))]
    (tc {:component :box
         :opts      {:height  (str h-body "px")
                     :ref     drop
                     :display "flex"
                     :width   width}
         :child     (map-indexed #(rum/with-key
                                    (wrap-box r %2 %1 (odd? index) code scale current-task prop)
                                    %1)
                                 list)})))

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

;TODO fix scroll position after scale
;(defn reset-cursor-scroll [old-scale scale]
;  (let [origin-old     (* old-scale 1439)
;        origin-new     (* scale 1439)
;        parent-element (.-parentNode (.-parentNode (.-current middle-list-ref)))
;        parent-width   (.-clientWidth parent-element)
;        scroll-left    (.-scrollLeft parent-element)
;        new-scroll     (* origin-new (/ scroll-left (- origin-old parent-width)))]
;    (print scroll-left parent-width)
;    (.scrollTo parent-element
;               (clj->js {:left new-scroll}))))

(defn zoom [r e scale]
  (let [delta        (.-deltaY e)
        window-event (.-event js/window)
        ctrl?        (.-ctrlKey window-event)]
    (when ctrl? (do (if (> delta 0)
                      (citrus/dispatch! r :home :dec-scale)
                      (citrus/dispatch! r :home :inc-scale))
                    ))))

(rum/defc body < rum/reactive
                 scroll-mixin
                 {:key-fn (fn [_] "body")}
  [r h-top h-header h-body]
  (let [list         (rum/react (citrus/subscription r [:chart :list]))
        chart        (rum/react (citrus/subscription r [:chart :chart]))
        scale        (rum/react (citrus/subscription r [:home :scale]))
        current-task (rum/react (citrus/subscription r [:chart :current-task]))]
    (citrus/dispatch! r :home :set-middle-list-ref middle-list-ref)
    (reset! last-active-time->ref {:position nil})
    (tc {:component :box
         :opts      {:overflow "hidden"
                     :ref      middle-list-ref
                     :onWheel  #(zoom r % scale)
                     :height   (str "calc(100vh - " (+ 2 h-top (* 2 h-header)) "px)")}
         :child     (map-indexed #(rum/with-key
                                    (item r %2 h-body %1 chart scale current-task)
                                    %1)
                                 list)})))

(rum/defc ChartList
  [r h-top h-header h-body]
  (vector (timeline/Timeline r h-header true)
          (body r h-top h-header h-body)
          (popper/Popper r)
          (timeline/Timeline r h-header false)))
