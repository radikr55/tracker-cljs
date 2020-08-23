(ns app.renderer.forms.home
  (:require [rum.core :as rum]
            [app.renderer.forms.home.header :as header]
            ["@material-ui/core" :refer [Box Portal]]
            [app.renderer.forms.home.tasks :as tasks]
            [app.renderer.forms.home.statistic :as statistic]
            [app.renderer.forms.search :as search]
            [app.renderer.forms.home.chart-box :as chart-box]))

(rum/defc Home < rum/reactive
  [r]
  [
   (rum/with-key (header/Header r) "home-header")
   (rum/with-key (chart-box/Chart-box r) "home-chart")
   (rum/adapt-class Box {:display         "flex"
                         :justify-content "space-between"}
                    [(rum/with-key (tasks/Tasks r) "home-tasks")
                     (rum/with-key (statistic/Statistic r) "home-statistic")])
   ])
