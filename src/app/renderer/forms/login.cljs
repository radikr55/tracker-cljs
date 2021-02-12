(ns app.renderer.forms.login
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc]]
            [citrus.core :as citrus]))

(rum/defc Login < rum/reactive
  [r]
  (tc {:component :box
       :opts      {:className "login-container"}
       :child     {:component :box
                   :opts      {:className "login-box"}
                   :child     {:component :button
                               :opts      {:variant   "contained"
                                           :key       "submit"
                                           :type      "submit"
                                           :color     "primary"
                                           :className "login-button"
                                           :onClick   #(citrus/dispatch! r :user :get-link)
                                           :fullWidth true}
                               :child     "Sign in to Jira"}}}))
