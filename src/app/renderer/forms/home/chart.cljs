(ns app.renderer.forms.home.chart
  (:require [rum.core :as rum]
            ["@material-ui/core" :refer [Paper Box]]
            [app.renderer.forms.home.timeline :as t]
            [citrus.core :as citrus]))

(rum/defc Chart < rum/reactive
  [r height]
  (let [data (rum/react (citrus/subscription r [:chart]))]
    (rum/adapt-class Box
                     {:display "flex"
                      :height  (str height "px")
                      :width   "100%"}
                     (map-indexed #(rum/adapt-class Box {:flex    (str "0 0 " (str (:interval %2) "px"))
                                                         :key     (str "task-" %1)
                                                         :height  "100%"
                                                         :bgcolor "primary.main"}
                                                    (:task-code %2)) data))))


