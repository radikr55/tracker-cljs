(ns app.renderer.forms.home.statistic
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc]]
            [app.renderer.forms.home.statistic.header :as header]
            [app.renderer.forms.home.statistic.tabs :as tabs]
            ))

(defn structure [header body]
  {:component :box
   :opts      {:p 1 :width "60%"}
   :child     {:component :paper
               :opts      {:elevation 2}
               :child     [{:component :box
                            :opts      { :pt 1 :width "100%"}
                            :child     header}
                           {:component :divider}
                           {:component :box
                            :opts      {:width "100%"}
                            :child     {:component :paper
                                        :opts      {:elevation 2}
                                        :child     body}}]}})

(rum/defc Statistic < rum/reactive
  [r]
  (tc (structure (header/Header r) (tabs/Tabs r))))

