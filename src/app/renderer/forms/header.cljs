(ns app.renderer.forms.header
  (:require [rum.core :as rum]
            ["electron" :refer [remote]]
            [citrus.core :as citrus]
            ["@material-ui/core/styles" :refer [styled]]
            ["@material-ui/core" :refer [IconButton Icon LinearProgress]]
            ["@material-ui/icons" :refer [Brightness4 Brightness7]]
            [frameless-titlebar :default TitleBar]))

(def currentWindow (.getCurrentWindow remote))

(rum/defc button-icon < rum/reactive
  [r]
  (let  [dark? (rum/react (citrus/subscription r [:theme]))]
    (if dark?
      (js/React.createElement Brightness7
                              (clj->js {:className "small-icon"}))
      (js/React.createElement Brightness4
                              (clj->js {:className "small-icon"})))))

(rum/defc theme-button [r]
  (js/React.createElement IconButton
                          (clj->js {:onClick   #(citrus/dispatch! r :theme :switch)
                                    :className "white"})
                          (button-icon r)))

;; (rum/defc loading  < rum/reactive
;;   [r]
;;   (let  [loading? (rum/react (citrus/subscription r [:loading]))
;;          style    (clj->js {:background-color "black"})]
;;     (js/React.createElement
;;       (
;;        (styled LinearProgress)
;;        (clj->js {:root {:background-color "black"}})
;;        )
;;       )
;;     )
;;   )

(rum/defc loading  < rum/reactive
  [r]
  (let  [loading? (rum/react (citrus/subscription r [:loading]))]
    (js/React.createElement LinearProgress
                            (clj->js {:className (if loading? "loading-off" )}))))

(rum/defc title-bar [r]
  (js/React.createElement TitleBar
                          (clj->js {:platform      (.-platform js/process)
                                    :onMinimize    (fn [] (.minimize currentWindow))
                                    :onDoubleClick (fn [] ())
                                    :onMaximize    (fn [] (if (.isMaximized currentWindow)
                                                            (.restore currentWindow)
                                                            (.maximize currentWindow)))
                                    :onClose       (fn [] (.close currentWindow))})
                          (theme-button r)))

(rum/defc Header [r]
  [:div
   (title-bar r)
   (loading r)])
