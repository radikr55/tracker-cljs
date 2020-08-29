(ns app.renderer.forms.home.chart.plus-line
  (:require [rum.core :as rum]
            ["@material-ui/core" :refer [Paper Box IconButton Typography]]
            [app.renderer.utils :refer [tc]]
            [cljs-time.core :as t]
            [cljs-time.format :as ft]
            [app.renderer.forms.home.chart.timeline :as tl]
            [app.renderer.forms.home.chart.chart :as c]
            [citrus.core :as citrus]))

(def plus-ref (js/React.createRef))
(def show-menu (atom false))
(def button (js/React.createRef))

(rum/defc menu < rum/reactive
  [r]
  (let [statistic (rum/react (citrus/subscription r [:home :statistic]))]
    (tc {:component :box
         :opts      {:p            2
                     :onMouseLeave #(reset! show-menu false)}
         :child     {:component :grid
                     :opts      {:spacing   2
                                 :container true}
                     :child     [{:component :grid
                                  :opts      {:item true
                                              :key  "start"
                                              :xs   6}
                                  :child     {:component :text-field
                                              :opts      {:label        "Start"
                                                          :defaultValue "07:30"
                                                          :type         "time"}}}
                                 {:component :grid
                                  :opts      {:item true
                                              :key  "end"
                                              :xs   6}
                                  :child     {:component :text-field
                                              :opts      {:label        "End"
                                                          :defaultValue "07:35"
                                                          :type         "time"}}}
                                 {:component :grid
                                  :opts      {:item true
                                              :key  "add"
                                              :xs   12}
                                  :child     {:component :button
                                              :opts      {:variant "contained"
                                                          :width   "100%"}
                                              :child     "Add tasks"}}]}})))

(rum/defc poper < rum/reactive
  {:key-fn (fn [_] "poper")}
  [r]
  (let [show-menu? (rum/react show-menu)]
    (tc {:component :popover
         :opts      {:open            show-menu?
                     :anchorEl        (.-current button)
                     :onClose         #(reset! show-menu false)
                     :anchorOrigin    {:vertical   "top"
                                       :horizontal "left"}
                     :transformOrigin {:vertical   "top"
                                       :horizontal "left"}}
         :child     {:component :paper
                     :opts      {:elevation 3}
                     :child     {:component :box
                                 :child     (menu r)}
                     }})))

(rum/defc tooltip-button < rum/reactive
  {:key-fn (fn [_] "button")}
  [r]
  (let [element (.-current  plus-ref)
        hide?   (rum/react (citrus/subscription r [:home :show-plus-line?]))
        mouse   (rum/react (citrus/subscription r [:home :mouse-position]))
        child   (tc {:component :icon-button
                     :opts      {:key     "icon-button"
                                 :ref     button
                                 :onClick #(do (citrus/dispatch! r :home :show-plus-line false)
                                               (reset! show-menu true))}
                     :child     {:component :add}})]
    (when (and hide? element) [:div {:style {:position "absolute"
                                             :top      (- (.-bottom (.getBoundingClientRect element)) 45)
                                             :left     (- (:pageX  mouse) 25)}}
                               child])))

(rum/defc plus-line < rum/reactive
  [r height]
  (let [theme (rum/react (citrus/subscription r [:theme :cljs]))
        paper (-> theme :palette :background :paper)]
    [:div {:ref          plus-ref
           :onMouseEnter #(citrus/dispatch! r :home :show-plus-line true)
           :onMouseLeave #(citrus/dispatch! r :home :show-plus-line false)
           :style        {:width            1440 ; 24*60
                          :height           height
                          :background-color paper}}
     (tooltip-button r)
     (poper r)]))

