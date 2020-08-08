(ns app.renderer.forms.home.statistic
  (:require [rum.core :as rum]
            ["@material-ui/core" :refer [Paper Box]]
            [citrus.core :as citrus]))

(defn to-box [child]
  (rum/adapt-class Box {:p     1
                        :width "100%"} child))

(defn paper [child]
(rum/adapt-class Paper {:elevation 3}
                 child))

(rum/defc Statistic < rum/reactive
  [r]
  (to-box ( paper "Statistic" )))

