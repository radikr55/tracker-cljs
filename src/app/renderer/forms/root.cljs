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

(def dark-theme
  (let [js-theme (createMuiTheme
                   (clj->js {:palette
                             (clj->js {:type      "dark"
                                       :primary   {:main "#90caf9"}
                                       :secondary {:main "#ff4081"}
                                       :devider   "rgba(255, 255, 255, 0.12)"})}))]
    {:js   js-theme
     :cljs (js->clj js-theme :keywordize-keys true)}))

(def light-theme
  (let [js-theme (createMuiTheme
                   (clj->js {:palette
                             (clj->js {:type      "light"
                                       :primary   {:main "#1976d2"}
                                       :secondary {:main "#c51162"}
                                       :devider   "rgba(255, 255, 255, 0.12)"})}))]
    {:js   js-theme
     :cljs (js->clj js-theme :keywordize-keys true)}))

(rum/defc Root < rum/reactive
  [r]
  (let  [route (rum/react (citrus/subscription r [:router]))
         token (js/localStorage.getItem "token")
         dark? (rum/react (citrus/subscription r [:theme]))
         theme (if dark? dark-theme light-theme)]
    (rum/react (citrus/subscription r [:refresh]))
    (citrus/dispatch! r :user :init-token token)
    (citrus/dispatch! r :home :set-theme theme)
    (rum/adapt-class
      MuiThemeProvider  {:theme (:js theme)}
      (js/React.createElement CssBaseline)
      (rum/with-key (Header r) "header")
      (Search r)
      ;; (case route
      ;;   :login  (Login r)
      ;;   :search (Search r)
      ;;   :home   (Home r)
      ;;   (if (boolean token)
      ;;     (Search r)
      ;;     (Login r)))
      )))
