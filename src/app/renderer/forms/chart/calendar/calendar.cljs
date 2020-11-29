(ns app.renderer.forms.chart.calendar.calendar
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [app.renderer.utils :refer [tc]]
            [app.renderer.forms.chart.calendar.header :refer [Header]]
            [app.renderer.forms.chart.calendar.body :refer [Body]]
            [app.renderer.forms.chart.calendar.stat :refer [Stat]]
            ))


(rum/defc Calendar < rum/reactive
  {:key-fn (fn [_] "calendar")}
  [r]
  (let [{open?    :open?
         position :position} (rum/react (citrus/subscription r [:calendar-popper]))]
    (tc {:component :popover
         :opts      {:open            open?
                     :anchorReference "anchorPosition"
                     :anchorPosition  {:top  (:mouseY position)
                                       :left (:mouseX position)}
                     :onClose         #(citrus/dispatch! r :calendar-popper :close-popper)}
         :child     {:component :paper
                     :opts      {:className "calendar-paper"}
                     :child     {:component :box
                                 :opts      {:className "calendar-box"}
                                 :child     [(Header r)
                                             {:component :box
                                              :opts      {:key       "calendar-body-stat"
                                                          :className "calendar-body-stat"}
                                              :child     [(Body r)
                                                          (Stat r)]}]
                                 }}})))
