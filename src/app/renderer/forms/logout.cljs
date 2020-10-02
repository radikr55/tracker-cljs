(ns app.renderer.forms.logout
  (:require [rum.core :as rum]
            ["electron" :refer [remote]]
            [citrus.core :as citrus]
            [app.renderer.utils :refer [tc]]))

(rum/defc dialog-logout < rum/reactive  [r]
  (tc {:component :dialog
       :opts      {:open    (rum/react (citrus/subscription r [:home :logout]))
                   :onClose #(citrus/subscription r :home :close-logout)}
       :child     [{:component :dialog-title
                    :opts      {:key "dialog-title"}
                    :child     "Logout"}
                   {:component :dialog-content
                    :opts      {:key "dialog-conent"}
                    :child     "Are you sure you want to sign out?"}
                   {:component :dialog-actions
                    :opts      {:key "dialog-action"}
                    :child     [{:component :icon-button
                                 :opts      {:key     "dialog-check"
                                             :onClick #(do (citrus/dispatch! r :user :logout r)
                                                           (citrus/dispatch! r :home :close-logout))}
                                 :child     {:component :check}}
                                {:component :icon-button
                                 :opts      {:key     "dialog-cancel"
                                             :onClick #(citrus/dispatch! r :home :close-logout)}
                                 :child     {:component :cancel}}]}]}))
