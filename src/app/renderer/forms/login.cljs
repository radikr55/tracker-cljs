(ns app.renderer.forms.login
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            ))

(rum/defc Login < rum/reactive
  [r]
  [:div [:input {:placeholder "test"
                 :on-change
                 #(let [val (.. % -target -value)]
                    (citrus/dispatch! r :user :login val))}]

   [:span (rum/react (citrus/subscription r [:user]))]
   [:button {:on-click #(citrus/dispatch! r :user :login @test)} "Login"]])
