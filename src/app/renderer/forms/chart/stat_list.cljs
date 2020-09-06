(ns app.renderer.forms.chart.stat-list
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc on-wheel-vertical]]
            [citrus.core :as citrus]))

(def right-list-ref (js/React.createRef))

(rum/defc header < {:key-fn (fn [_ x] "header")}
  [h-header]
  (tc {:component :box
       :opts      {:height    (str h-header "px")
                   :className "bottom-border"
                   :width     "100%"}}))

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

(rum/defc item < rum/reactive
  {:key-fn (fn [_ row] (:code row))}
  [r row h-body]
  (tc {:component :box
       :opts      {:height    (str h-body "px")
                   :className "stat-box"}
       :child     (:format row)}))

(rum/defc body < rum/reactive
  {:key-fn (fn [_] "body")}
  [r h-top h-header h-body]
  (let [middle-list-ref (rum/react (citrus/subscription r [:home :middle-list-ref]))
        left-list-ref   (rum/react (citrus/subscription r [:home :left-list-ref]))
        list            (rum/react (citrus/subscription r [:chart :list]))]
    (citrus/dispatch! r :home :set-right-list-ref right-list-ref)
    (tc {:component :box
         :opts      {:overflow "hidden"
                     :className "right-list"
                     :ref      right-list-ref
                     :onWheel  #(on-wheel-vertical % [middle-list-ref
                                                       left-list-ref
                                                       right-list-ref])
                     :height   (str "calc(100vh - " (+ 2 h-top (* 2 h-header)) "px)")}
         :child     (for [row list]
                      (item r row h-body))})))

(rum/defc StatList < rum/reactive
  [r h-top h-header h-body]
  (vector (header h-header)
          (body r h-top h-header h-body)
          (footer r h-header)))

