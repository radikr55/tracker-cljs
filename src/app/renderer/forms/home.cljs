(ns app.renderer.forms.home
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc]]
            [app.renderer.forms.chart.header :as header]
            [app.renderer.forms.chart.table :as table]))

(rum/defc Home < rum/reactive
  [r]
  (let [h-header 65]
    (tc {:component :box
         :child     [(header/Header r h-header)
                     (table/Table r h-header)]})))
