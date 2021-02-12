(ns app.renderer.forms.chart.calendar.header
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [app.renderer.utils :refer [tc]]
            [app.renderer.time-utils :as tu]))

(rum/defc title < rum/reactive
                  {:key-fn (fn [_] "title")}
  [r]
  (let [date (rum/react (citrus/subscription r [:calendar-popper :date]))]
    (tc {:component :typography
         :opts      {:className "calendar-header-title"}
         :child     (tu/date->calendar-title date)})))

(rum/defc buttons < rum/reactive
                    {:key-fn (fn [_] "buttons")}
  [r]
  (tc {:component :box
       :child     [{:component :icon-button
                    :opts      {:onClick #(citrus/dispatch! r :calendar-popper :previously-month r)
                                :key     "previously"}
                    :child     {:component :arrow-left}}
                   {:component :icon-button
                    :opts      {:onClick #(citrus/dispatch! r :calendar-popper :next-month r)
                                :key     "next"}
                    :child     {:component :arrow-right}}]}))

(rum/defc Header < rum/reactive
                   {:key-fn (fn [_] "header")}
  [r]
  (tc {:component :box
       :opts      {:className "calendar-header"}
       :child     [(title r)
                   (buttons r)]}))
