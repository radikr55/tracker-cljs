(ns app.renderer.forms.chart.submenu
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc]]
            [citrus.core :as citrus]))

(rum/defc SubMenu < rum/reactive
  [r]
  (let [p     (rum/react (citrus/subscription r [:home :position-submenu]))
        open? (rum/react (citrus/subscription r [:home :open-left]))]
    (when (not (nil? (:mouseX p)))
      (tc
        {:component :menu
         :opts      {:open            open?
                     :onClose         #(citrus/dispatch! r :home :close-submenu)
                     :anchorReference "anchorPosition"
                     :anchorPosition  {:top  (:mouseY p)
                                       :left (:mouseX p)}
                     :keepMounted     true
                     :className       "submenu"
                     :variant         "selectedMenu"}
         :child     [{:component :menu-item
                      :opts      {:key "view"}
                      :child     [{:component :list-item-icon
                                   :opts      {:key "icon"}
                                   :child     {:component :open-in-new
                                               :opts      {:className "menu-item-icon"}}}
                                  {:component :typography
                                   :opts      {:className "menu-item-typography"
                                               :key       "typography"}
                                   :child     "View in JIRA"}]}
                     {:component :menu-item
                      :opts      {:key "remove"}
                      :child     [{:component :list-item-icon
                                   :opts      {:key "icon"}
                                   :child     {:component :close
                                               :opts      {:className "menu-item-icon"}}}
                                  {:component :typography
                                   :opts      {:key       "typography"
                                               :className "menu-item-typography"}
                                   :child     "Remove Task"}]}]}))))

