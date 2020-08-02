(ns app.renderer.forms.home.chart-box
  (:require [rum.core :as rum]
            ["@material-ui/core" :refer [Paper Box]]
            [app.renderer.forms.home.timeline :as t]
            [app.renderer.forms.home.chart :as c]
            [citrus.core :as citrus]))

(def  ref (rum/create-ref))

(defn to-box [child]
  (rum/adapt-class Box {:p 1} child))

(defn paper [child]
  (rum/adapt-class Paper {:elevation 3}
                   child))

(defn on-wheel-container [e]
  (let [delta  (.-deltaY e)
        scroll (.-scrollLeft (.-current ref))]
    (if (> delta 0)
      (set! (.-scrollLeft (.-current ref)) (+ scroll 50))
      (set! (.-scrollLeft (.-current ref)) (- scroll 50)))))

(rum/defc container < rum/reactive
  [r]
  (rum/adapt-class Box {:p 1}
                   (rum/adapt-class Box {:onWheel  on-wheel-container
                                         :ref      ref
                                         :overflow "hidden"}
                                    [(c/Chart r)
                                     (t/Timeline r)])))

(rum/defc Chart-box < rum/reactive
  [r]
  (to-box (paper
            (container r))))
