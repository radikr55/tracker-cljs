(ns app.renderer.forms.home.statistic.header
  (:require [app.renderer.utils :refer [tc]]
            [citrus.core :as citrus]
            ["@material-ui/core" :refer [IconButton]]
            [rum.core :as rum]))

(def show-menu (atom false))
(def button (js/React.createRef))

(rum/defc switch-menu < rum/reactive
  [r type]
  (let [statistic (rum/react (citrus/subscription r [:home :statistic]))]
    (tc {:component :switch
         :opts      {:checked  (contains? statistic type)
                     :onChange #(citrus/dispatch! r :home :switch-board type)}})))

(rum/defc menu < rum/reactive
  [r]
  (let [statistic (rum/react (citrus/subscription r [:home :statistic]))]
    (tc {:component :box
         :opts      {:pl 2}
         :child     {:component :form-control
                     :opts      {:component "fieldset"}
                     :child     {:component :form-group
                                 :child     [{:component :form-control-label
                                              :opts      {:control  (switch-menu r :logged)
                                                          :disabled (< (count statistic) 2)
                                                          :label    "Logged"}}
                                             {:component :form-control-label
                                              :opts      {:control  (switch-menu r :tracked)
                                                          :disabled (< (count statistic) 2)
                                                          :label    "Tracked"}}
                                             {:component :form-control-label
                                              :opts      {:control  (switch-menu r :submitted)
                                                          :disabled (< (count statistic) 2)
                                                          :label    "Submitted"}}]}}})))

(rum/defc poper < rum/reactive
  [r]
  (let [show (rum/react show-menu)]
    (tc {:component :popover
         :opts      {:open            show
                     :anchorEl        (.-current button)
                     :onClose         #(reset! show-menu false)
                     :anchorOrigin    {:vertical   "bottom"
                                       :horizontal "left"}
                     :transformOrigin {:vertical   "top"
                                       :horizontal "left"}}
         :child     {:component :paper
                     :opts      {:elevation 3}
                     :child     {:component :box
                                 :child     (menu r)}}})))

(rum/defc Header < rum/reactive
  [r]
  (tc {:component :box
       :opts      {:px             2
                   :display        "flex"
                   :justifyContent "space-between"
                   :alignItems     "center"}
       :child     [{:component :typography
                    :opts      {:variant "h6"}
                    :child     "Statistic"}
                   (poper r)
                   {:component :icon-button
                    :opts      {:onClick #(reset! show-menu (not @show-menu))
                                :ref     button}
                    :child     {:component :setting-sharp}}]}))

