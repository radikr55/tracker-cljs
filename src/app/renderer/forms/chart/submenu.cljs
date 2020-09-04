(ns app.renderer.forms.chart.submenu
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc]]
            [citrus.core :as citrus]))

(rum/defc SubMenu < rum/reactive
  [r]
  (let [p (rum/react (citrus/subscription r [:home :position-submenu]))]
    (tc
      {:component :menu
       :opts      {:open            (not (nil? (:mouseX p)))
                   :onClose         #(citrus/dispatch! r :home :close-submenu)
                   :anchorReference "anchorPosition"
                   :anchorPosition  {:top  (:mouseY p)
                                     :left (:mouseX p)}
                   :keepMounted     true
                   :variant "selectedMenu"}
       :child     [{:component :menu-item
                    :child     [{:component :list-item-icon
                                 :child     {:component :open-in-new
                                             :opts      {:className "menu-item-icon"}}}
                                {:component :typography
                                 :opts      {:className "menu-item-typography"}
                                 :child     "View in JIRA"}]}
                   {:component :menu-item
                    :child     [{:component :list-item-icon
                                 :child     {:component :close
                                             :opts      {:className "menu-item-icon"}}}
                                {:component :typography
                                 :opts      {:className "menu-item-typography"}
                                 :child     "Remove Task"}]}]})))

