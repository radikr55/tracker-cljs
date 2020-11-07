(ns app.renderer.forms.chart.right.stat-list
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc on-wheel-vertical]]
            [app.renderer.time-utils :as tu]
            [app.renderer.forms.chart.right.stat-popper :as stat-popper]
            [citrus.core :as citrus]))

(def right-list-ref (js/React.createRef))

(rum/defc header < {:key-fn (fn [_ x] "header")}
  [h-header]
  (tc {:component :box
       :opts      {:height    (str h-header "px")
                   :className "bottom-border stat-header"
                   :width     "100%"}
       :child     "Total"}))

(rum/defc footer < rum/reactive
  {:key-fn (fn [_ x] "footer")}
  [r h-header]
  (tc {:component :box
       :opts      {:height    (str h-header "px")
                   :className "top-border stat-footer"
                   :width     "100%"}
       :child     [{:component :icon-button
                    :opts      {:key     "zoomOut"
                                :onClick #(citrus/dispatch! r :home :dec-scale)}
                    :child     {:component :zoom-out}}
                   {:component :icon-button
                    :opts      {:key     "zoomIn"
                                :onClick #(citrus/dispatch! r :home :inc-scale)}
                    :child     {:component :zoom-in}}]}))

(defn open-menu [r event code field]
  (citrus/dispatch! r :stat-popper :open-popper
                    {:position {:mouseX (.-clientX event)
                                :mouseY (.-clientY event)}
                     :code     code
                     :time     (/ field 60)}))

(rum/defc item < rum/reactive
  {:key-fn (fn [_ row] (:code row))}
  [r row h-body index]
  (let [code         (:code row)
        away?        (seq code)
        interval     (:interval row)
        current-task (rum/react (citrus/subscription r [:chart :current-task]))
        class        (cond-> "stat-box "
                       (empty? code)         (str " away ")
                       (seq code)            (str " stat-blue ")
                       (odd? index)          (str " chart-block-gray ")
                       (= 0 interval)        (str " stat-block-empty ")
                       (= code current-task) (str " selected-row "))
        context-menu (when away?
                       #(open-menu r % code (:format-field row)))
        submitted    (->> (rum/react (citrus/subscription r [:chart :desc]))
                          (filter #(= code (:code %)))
                          first
                          :submitted)]
    (tc {:component :box
         :opts      {:height    (str h-body "px")
                     :onClick   context-menu
                     :className class}
         :child     [{:component :box
                      :child     (:format row)}
                     (when (and away? (not (= 0 submitted)))
                       {:component :box
                        :opts      {:className "submitted"}
                        :child     (tu/format-time (/ submitted 60))})]})))

(rum/defc body < rum/reactive
{:key-fn (fn [_] "body")}
  [r h-top h-header h-body]
  (let [middle-list-ref (rum/react (citrus/subscription r [:home :middle-list-ref]))
        left-list-ref   (rum/react (citrus/subscription r [:home :left-list-ref]))
        list            (rum/react (citrus/subscription r [:chart :list]))]
    (citrus/dispatch! r :home :set-right-list-ref right-list-ref)
    (tc {:component :box
         :opts      {:overflow  "hidden"
                     :className "right-list"
                     :ref       right-list-ref
                     :onWheel   #(on-wheel-vertical % [middle-list-ref
                                                       left-list-ref
                                                       right-list-ref])
                     :height    (str "calc(100vh - " (+ 2 h-top (* 2 h-header)) "px)")}
         :child     (map-indexed #(item r %2 h-body %1) list)})))

(rum/defc StatList < rum/reactive
  [r h-top h-header h-body]
  (vector (header h-header)
          (body r h-top h-header h-body)
          (stat-popper/Popper r)
          (footer r h-header)))

