(ns app.renderer.forms.chart.table
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc]]
            [citrus.core :as citrus]))

(rum/defc left < rum/reactive
  [r]
  (tc {:component :box
       :opts      {:width   (str  (rum/react (citrus/subscription r [:home :left-size])) "px")
                   :height  "100%"
                   :bgcolor "yellow"}}))

(rum/defc middle < rum/reactive
  [r]
  (tc {:component :box
       :opts      {:width   "100%"
                   :height  "100%"
                   :bgcolor "gray"}}))

(rum/defc gap < rum/reactive
  [r]
  (tc {:component :draggable
       :opts      {:axis     "x"
                   :position {:x (rum/react (citrus/subscription r [:home :left-size]))}
                   :onDrag   #(citrus/dispatch! r :home :set-left-size  (.-deltaX %2) )}
       ;; :onDrag    #(print (.-pageX %))}
       :child     {:component :box
                   :styl      {:cursor "ew-resize"}
                   :opts      {:width          "10px"
                               :height         "100%"
                               :display        "flex"
                               :flexDirection  "column"
                               :justifyContent "center"
                               :alignItems     "center"
                               :bgcolor        "green"
                               :onDragStart    #(print %)}
                   :child     [{:component :dot
                                :styl      {:width "10px"}}
                               {:component :dot
                                :styl      {:width "10px"}}
                               {:component :dot
                                :styl      {:width "10px"}}]} }))

(rum/defc right < rum/reactive
  [r]
  (tc {:component :box
       :opts      {:bgcolor   "white"
                   :flex "0 0 50px"
                   ;; :flexBasis "50px"
                   :height    "100%"}}))

(rum/defc Table < rum/reactive
  [r]
  (tc {:component :box
       :opts      {:height  "300px"
                   :display "flex"}
       :child     [(left r)
                   (gap r)
                   (middle r)
                   (right r)]}))
