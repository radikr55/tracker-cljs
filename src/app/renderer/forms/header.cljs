(ns app.renderer.forms.header
  (:require [rum.core :as rum]
            ["electron" :refer [remote]]
            [app.renderer.forms.logout :refer [Logout]]
            [citrus.core :as citrus]
            ["@material-ui/core" :refer [IconButton Icon LinearProgress Fade]]
            ["@material-ui/icons" :refer [Brightness4 Brightness7 RefreshOutlined]]
            [frameless-titlebar :default TitleBar]))

(def currentWindow (.getCurrentWindow remote))

(rum/defc button-icon < rum/reactive
  [r]
  (let  [dark? (rum/react (citrus/subscription r [:theme]))]
    (if dark?
      (rum/adapt-class Brightness7
                       {:className "small-icon"})
      (rum/adapt-class Brightness4
                       {:className "small-icon"}))))

(rum/defc theme-button [r]
  (rum/adapt-class     IconButton
                       {:key       "icon-button"
                        :onClick   #(citrus/dispatch! r :theme :switch)
                        :className "white"
                        :title     "Switch theme"}
                       (button-icon r)))

;; (rum/defc refresh-button [r]
;;   (rum/adapt-class     IconButton
;;                        {:key       "icon-button"
;;                         :onClick   #(reset! refresh-atom true)
;;                         :className "white"
;;                         :title     "Refresh"}
;;                        (rum/adapt-class   RefreshOutlined
;;                                           {:className "small-icon"})))

(rum/defc loading  < rum/reactive
  [r]
  (let  [loading? (rum/react (citrus/subscription r [:loading]))]
    (rum/adapt-class Fade
                     {:in (not loading?)}
                     (rum/adapt-class LinearProgress
                                      {}))))

(rum/defc title-bar [r]
  (rum/adapt-class  TitleBar
                    {:key           "title-bar"
                     :platform      (.-platform js/process)
                     :onMinimize    (fn [] (.minimize currentWindow))
                     :onDoubleClick (fn [] ())
                     :onMaximize    (fn [] (if (.isMaximized currentWindow)
                                             (.restore currentWindow)
                                             (.maximize currentWindow)))
                     :onClose       (fn [] (.close currentWindow))}
                    [
                     ;; (rum/with-key (refresh-button r) "refresh-button")
                     (rum/with-key (theme-button r) "theme-button")
                     (rum/with-key (Logout r) "logout-button")]))

(rum/defc Header [r]
  [:div
   (rum/with-key (title-bar r) "header-titlebar")
   (rum/with-key (loading r) "header-loader")])
