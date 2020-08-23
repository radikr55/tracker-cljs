(ns app.renderer.forms.home.chart.header
  (:require [app.renderer.utils :refer [tc]]
            [citrus.core :as citrus]
            [rum.core :as rum]))

(rum/defc Header < rum/reactive
  [r]
  (let [all            (rum/react (citrus/subscription r [:chart :chart]))
        selected       (rum/react (citrus/subscription r [:chart :selected]))
        order          (rum/react (citrus/subscription r [:chart :order]))
        current        (rum/react (citrus/subscription r [:chart :current]))
        all-codes      (set (filter #(not (nil? %)) (map :code  all)))
        all-visibility (every? selected all-codes)]
    (tc {:component :box
         :opts      {:px             2
                     :display        "flex"
                     :justifyContent "space-between"
                     :alignItems     "center"}
         :child     [{:component :typography
                      :opts      {:variant "h6"
                                  :display "flex"}
                      :child     "Current task: "}
                     {:component :typography
                      :opts      {:variant "h6"
                                  :display "flex"}
                      :child     current}
                     {:component :box
                      :child     [{:component :icon-button
                                   :opts      {:onClick #(citrus/dispatch! r :chart :switch-all-selected all-codes)}
                                   :child     {:component (if all-visibility
                                                            :visibility
                                                            :visibility-outlined)}}]}]})))

