(ns app.renderer.forms.home
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc]]
            [app.renderer.forms.chart.header :as header]
            [app.renderer.forms.chart.table :as table]
            ))

(rum/defc Home < rum/reactive
  [r]
  (tc {:component :box
       :child     [(header/Header r)
                   (table/Table r)
                   ]})
  ;; [
  ;;  (rum/with-key (header/Header r) "home-header")
  ;;  (rum/with-key (chart-box/Chart-box r) "home-chart")
  ;;  (rum/adapt-class Box {:display         "flex"
  ;;                        :key             "box"
  ;;                        :justify-content "space-between"}
  ;;                   [(rum/with-key (tasks/Tasks r) "home-tasks")
  ;;                    (rum/with-key (statistic/Statistic r) "home-statistic")])
  ;;  ]
  )
