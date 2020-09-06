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


(rum/defc Root <  rum/reactive
  [r]
  (let  [route (rum/react (citrus/subscription r [:router]))
         token (js/localStorage.getItem "token")]
    (rum/react (citrus/subscription r [:refresh]))
    (citrus/dispatch! r :user :init-token token)
    [:div (js/React.createElement CssBaseline)
     (case route
       :login  (Login r)
       :search (Search r)
       :home   (Home r)
       (if (boolean token)
         (Home r)
         (Login r)))]
    ))
