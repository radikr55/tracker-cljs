(ns app.renderer.forms.root
  (:require [rum.core :as rum]
            [app.renderer.forms.login :refer [Login]]
            [app.renderer.forms.home :refer [Home]]
            [citrus.core :as citrus]
            ["@material-ui/core" :refer [Button CssBaseline Typography]]
            ["@material-ui/core/styles" :refer [createMuiTheme MuiThemeProvider]]
            ["@material-ui/core/styles/createPalette" :default createPalette]
            [app.renderer.forms.header :refer [Header]]
            [app.renderer.forms.search :refer [Search]]))

(defn create-theme [dark?]
  (createMuiTheme
    (clj->js {:palette
              (clj->js {:type      (if dark? "dark" "light")
                        :primary   {:main (if dark? "#90caf9" "#1976d2")}
                        :secondary {:main (if dark? "#ff4081" "#c51162")}
                        :devider   "rgba(255, 255, 255, 0.12)"})})))

(rum/defc Root < rum/reactive
  [r]
  (let  [{route :handler} (rum/react (citrus/subscription r [:router]))
         token            (js/localStorage.getItem "token")
         dark?            (rum/react (citrus/subscription r [:theme]))
         theme            (create-theme dark?)]
    (rum/react (citrus/subscription r [:refresh]))
    (citrus/dispatch! r :user :init-token token)
    (citrus/dispatch! r :home :set-theme theme)
    (rum/adapt-class
      MuiThemeProvider  {:theme theme}
      (js/React.createElement CssBaseline)
      (rum/with-key (Header r) "header")
      (Home r)
      ;; (case route
      ;;   :login (Login r)
      ;;   :home  (Home r)
      ;;   (if (boolean token)
      ;;     (Search r)
      ;;     (Login r))
      )))
