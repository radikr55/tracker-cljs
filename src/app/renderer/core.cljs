(ns app.renderer.core
  (:require [rum.core :as rum]
            ["electron" :refer [remote]]
            [goog.dom :as dom]
            [citrus.core :as citrus]
            [app.renderer.effects :as effects]
            [app.renderer.controllers.user :as user]
            [app.renderer.controllers.loading :as loading]
            [app.renderer.controllers.theme :as theme]
            [app.renderer.forms.root :refer [Root]]))


(enable-console-print!)

(defonce reconciler
  (citrus/reconciler
    {:state           (atom {})
     :controllers     {:loading loading/control
                       :user    user/control
                       :theme   theme/control}
     :effect-handlers {:local-storage effects/local-storage}}))

;; initialize controllers
(defonce init-ctrl (citrus/broadcast-sync! reconciler :init))

;; (defn ^:dev/before-load stop []
;;   (js/console.log "stop"))

(citrus/dispatch! reconciler :user :login {:login "12312" :password "13123"})

(citrus/dispatch! reconciler :user :change {:login "asdfates123t" :password "1pass"})

(citrus/dispatch! reconciler :loading :on)

(citrus/dispatch! reconciler :loading :off)

(defn start! []
  (rum/mount (Root reconciler)
             (dom/getElement "app-container"))
  )

(defn ^:dev/after-load start []
  (start!))
