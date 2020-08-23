(ns app.renderer.forms.home.tasks
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc]]
            [app.renderer.forms.home.tasks.list :as list]
            [app.renderer.forms.home.tasks.header :as header]
            [citrus.core :as citrus]))

(defn structure [header body]
  {:component :box
   :opts      {:p 1 :width "100%"}
   :child     {:component :paper
               :opts      {:elevation 1}
               :child     [{:component :box
                            :opts      {:pt    1
                                        :width "100%"
                                        :key   "header"}
                            :child     header}
                           {:component :divider
                            :opts      {:key "divider"}}
                           {:component :box
                            :opts      {:width "100%"
                                        :key   "body"}
                            :child     {:component :paper
                                        :opts      {:elevation 2}
                                        :child     body}}]}})

(rum/defc Tasks < rum/reactive
  [r]
  (tc  (structure (header/Header r) (list/Tasks-list r))))

