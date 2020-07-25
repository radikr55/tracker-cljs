(ns app.renderer.forms.root
  (:require [rum.core :as rum]
            [app.renderer.forms.login :refer [Login]]
            [citrus.core :as citrus]
            ["@date-io/moment" :as MomentUtils]
            ["@material-ui/core" :refer [Button CssBaseline Typography]]
            ["@material-ui/pickers" :refer [MuiPickersUtilsProvider KeyboardDatePicker]]
            ["@material-ui/core/styles" :refer [createMuiTheme MuiThemeProvider]]
            ["@material-ui/core/styles/createPalette" :default createPalette]
            [app.renderer.forms.header :refer [Header]]))

(defn theme [dark?]
  (createMuiTheme
    (clj->js {:palette
              (clj->js { :type     (if dark? "dark" "light")
                        :primary   {:main (if dark? "#90caf9" "#1976d2")}
                        :secondary {:main (if dark? "#ff4081" "#c51162")}
                        :devider   "rgba(255, 255, 255, 0.12)"} )})
    ))
;; (def picker
;;   (js/React.createElement KeyboardDatePicker
;;                           (clj->js {:variant  "inline"
;;                                     :margin   "normal"
;;                                     :format   "MM/dd/yyyy"
;;                                     :onChange #(print %)})))

;; (def provider
;;   (js/React.createElement MuiPickersUtilsProvider
;;                           (clj->js {:utils #(new MomentUtils %)})
;;                           picker))


(def csss
  (js/React.createElement CssBaseline))

(rum/defc theme-provider < rum/reactive
  [r]
  (let  [dark? (rum/react (citrus/subscription r [:theme]))]
    (js/React.createElement
      MuiThemeProvider (clj->js {:theme (theme dark?)})
      csss
      (Header r)
      (Login r))))

(rum/defc Root
  [r]
  (theme-provider r))
