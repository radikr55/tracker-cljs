(ns app.renderer.forms.chart.left.task-popper
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc]]
            ["electron" :refer [shell]]
            [citrus.core :as citrus]))

(rum/defc SubMenu < rum/reactive
                    {:key-fn (fn [_] "submenu")}
  [r]
  (let [p        (rum/react (citrus/subscription r [:task-popper]))
        code     (:code p)
        open?    (:open p)
        position (:position p)
        link     (:link p)]
    (when (not (nil? (:mouseX position)))
      (tc
        {:component :menu
         :opts      {:open            open?
                     :onClose         #(citrus/dispatch! r :task-popper :close-popper)
                     :anchorReference "anchorPosition"
                     :anchorPosition  {:top  (:mouseY position)
                                       :left (:mouseX position)}
                     :keepMounted     true
                     :className       "submenu"
                     :variant         "selectedMenu"}
         :child     [{:component :menu-item
                      :opts      {:key     "view"
                                  :onClick #(.openExternal shell link)}
                      :child     [{:component :list-item-icon
                                   :opts      {:key "icon"}
                                   :child     {:component :open-in-new
                                               :opts      {:className "menu-item-icon"}}}
                                  {:component :typography
                                   :opts      {:className "menu-item-typography"
                                               :key       "typography"}
                                   :child     "View in JIRA"}]}
                     {:component :menu-item
                      :opts      {:key     "remove"
                                  :onClick #(citrus/dispatch! r :chart :delete-current-task code)}
                      :child     [{:component :list-item-icon
                                   :opts      {:key "icon"}
                                   :child     {:component :close
                                               :opts      {:className "menu-item-icon"}}}
                                  {:component :typography
                                   :opts      {:key       "typography"
                                               :className "menu-item-typography"}
                                   :child     "Remove Task"}]}]}))))
