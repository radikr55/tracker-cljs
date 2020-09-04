(ns app.renderer.forms.chart.chart-list
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc]]
            [citrus.core :as citrus]
            [app.renderer.forms.chart.timeline :as timeline]))

(def middle-list-ref (js/React.createRef))

(rum/defc item < rum/reactive
  {:key-fn (fn [_ index] (str index))}
  [r index h-body]
  (tc {:component :box
       :styl      {:background-color (when (odd? index) "grey")}
       :opts      {:height (str h-body "px")}
       :child     index}))

(rum/defc ChartList < rum/reactive
  [r h-top h-header h-body]
  (citrus/dispatch! r :home :set-middle-list-ref middle-list-ref)
  (vector (timeline/Timeline r h-header true)
          (tc {:component :box
               :opts      {:overflow "hidden"
                           :ref      middle-list-ref
                           :width    "2880px"
                           :height   (str "calc(100vh - " (+ 2 h-top (* 2 h-header)) "px)")}
               :child     (for [x (range 0 50)]
                            (item r x h-body))})
          (timeline/Timeline r h-header false)))
