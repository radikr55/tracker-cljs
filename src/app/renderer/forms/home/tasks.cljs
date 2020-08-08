(ns app.renderer.forms.home.tasks
  (:require [rum.core :as rum]
            ["@material-ui/core" :refer [Paper
                                         Box
                                         List
                                         ListItem
                                         ListItemText]]
            [citrus.core :as citrus]))

(defn to-box [child]
  (rum/adapt-class Box {:p 1 :width "100%"} child))

(defn paper [child]
  (rum/adapt-class Paper {:elevation 3}
                   child))

(rum/defc list-item < rum/reactive
  [r]
  (rum/adapt-class ListItem {:button true}
                   (rum/adapt-class ListItemText
                                    {:primary "test" :secondary "testasdf"})))

(rum/defc table < rum/reactive
  [r child]
  (rum/adapt-class List {:component "nav"}
                   child))

(rum/defc Tasks < rum/reactive
  [r]
  (to-box (paper
            (table r (list-item r)))))

