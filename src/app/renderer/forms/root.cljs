(ns app.renderer.forms.root
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc]]
            [app.renderer.forms.login :refer [Login]]
            [app.renderer.forms.logout :refer [dialog-logout]]
            [app.renderer.forms.about :refer [dialog-about]]
            [app.renderer.forms.home :refer [Home]]
            [citrus.core :as citrus]
            ["@material-ui/core" :refer [CssBaseline]]
            ["@material-ui/core/styles" :refer [createMuiTheme MuiThemeProvider]]
            [app.renderer.forms.search :refer [Search]]))

(defn create-theme [dark?]
  (createMuiTheme
    (clj->js {:typography {:fontFamily ["Roboto-Bold"]}
              :palette    {:type      (if dark? "dark" "light")
                           :primary   {:main (if dark? "#90caf9" "#1976d2")}
                           :secondary {:main (if dark? "#ff4081" "#c51162")}
                           :devider   "rgba(255, 255, 255, 0.12)"}})))

(rum/defc loading < rum/reactive
  [r]
  (let [loading? (rum/react (citrus/subscription r [:loading]))]
    (tc {:component :fade
         :styl      {:position "absolute"
                     :top      0
                     :width    "100%"}
         :opts      {:in (not loading?)}
         :child     {:component :linear-progress}})))

(rum/defc snack < rum/reactive
  [r]
  (let [error (rum/react (citrus/subscription r [:error]))]
    (when (:code error)
      (tc {:component :snack
           :opts      (cond-> {:open    (:code error)
                               :onClose #(citrus/dispatch! r :error :init)}
                              (:auto-hide error) (merge {:autoHideDuration 5000}))
           :child     {:component :alert
                       :child     (:message error)
                       :opts      (cond-> {:severity (:severity error)
                                           :onClose  #(citrus/dispatch! r :error :init)}
                                          (:button error) (merge {:action (tc {:component :button
                                                                               :opts      {:onClick (:action error)}
                                                                               :child     (:button error)})}))}}))))

(rum/defc Root < rum/reactive
  [r]
  (let [route (rum/react (citrus/subscription r [:router]))
        token (js/localStorage.getItem "token")
        dark? (rum/react (citrus/subscription r [:theme :dark?]))
        theme (create-theme dark?)]
    (rum/react (citrus/subscription r [:refresh]))
    (citrus/dispatch! r :user :init-token token)
    (rum/adapt-class
      MuiThemeProvider {:theme theme}
      (js/React.createElement CssBaseline)
      [:div
       (dialog-logout r)
       (dialog-about r)
       (loading r)
       (snack r)
       (case route
         :login (Login r)
         :search (Search r)
         :home (Home r)
         (if (boolean token)
           (Home r)
           (Login r)))])))
