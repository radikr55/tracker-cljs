(ns app.renderer.forms.logout
  (:require [rum.core :as rum]
            ["electron" :refer [remote]]
            [citrus.core :as citrus]
            ["@material-ui/core" :refer [IconButton
                                         DialogTitle
                                         Dialog
                                         DialogContent
                                         DialogActions]]
            ["@material-ui/icons" :refer [ExitToApp CheckCircle Cancel]]))

(def open-dialog (atom false))

(rum/defc dialog-logout < rum/reactive  [r]
  (let [theme (rum/react (citrus/subscription r [:theme :cljs]))]
    (rum/adapt-class Dialog
                     {:open    (rum/react open-dialog)
                      :onClose #(reset! open-dialog false)}
                     [(rum/adapt-class    DialogTitle
                                          {:key "dialog-title"}
                                          "Logout")
                      (rum/adapt-class    DialogContent
                                          {:key "dialog-content"}
                                          "Are you sure you want to sign out?")
                      (rum/adapt-class    DialogActions
                                          {:key "dialog-action"}
                                          [(rum/adapt-class IconButton
                                                            {:key     "dialog-check"
                                                             :onClick (fn [e]
                                                                        (citrus/dispatch! r :user :logout r)
                                                                        (reset! open-dialog false))}
                                                            (rum/adapt-class CheckCircle {:style {:color (-> theme :palette :success :main)} }))
                                           (rum/adapt-class IconButton
                                                            {:key     "dialog-cancel"
                                                             :color   "secondary"
                                                             :onClick #(reset! open-dialog false)}
                                                            (rum/adapt-class  Cancel {}))])])))

(rum/defc Logout < rum/reactive [r]
  (let [{token :token} (rum/react (citrus/subscription r [:user]))]
    (if (boolean token)
      [(rum/adapt-class    IconButton
                           {:onClick   #(reset! open-dialog true)
                            :key       "logout-icon"
                            :className "white"
                            :title     "Logout"}
                           (rum/adapt-class    ExitToApp
                                               {:className "small-icon"}))
       (rum/with-key (dialog-logout r) "dialog-logout")])))
