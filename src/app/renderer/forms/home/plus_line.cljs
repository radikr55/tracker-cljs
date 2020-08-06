(ns app.renderer.forms.home.plus-line
  (:require [rum.core :as rum]
            ["@material-ui/core" :refer [Paper Box IconButton Typography]]
            ["@material-ui/icons" :refer [AddCircleOutline]]
            [cljs-time.core :as t]
            [cljs-time.format :as ft]
            [app.renderer.forms.home.timeline :as tl]
            [app.renderer.forms.home.chart :as c]
            [citrus.core :as citrus]))



(rum/defc plus-line < rum/reactive
  [r height]
  [:div {
         :onMouseEnter #(citrus/dispatch! r :home :plus-line true)
         :onMouseLeave #(citrus/dispatch! r :home :plus-line false)
         :style        {:width            1440 ; 24*60
                        :height           height
                        :background-color "white"}}])

