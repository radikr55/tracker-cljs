(ns app.renderer.core
  (:require [rum.core :as rum]
            ["electron" :refer [remote]]
            [goog.dom :as dom]
            [citrus.core :as citrus]
            [app.renderer.effects :as effects]
            [app.renderer.controllers.user :as user]
            [app.renderer.forms.root :refer [Root]]))


(enable-console-print!)

(defonce reconciler
  (citrus/reconciler
    {:state           (atom {})
     :controllers     {:user user/control}
     :effect-handlers {:local-storage effects/local-storage}}))

;; initialize controllers
(defonce init-ctrl (citrus/broadcast-sync! reconciler :init))

(defn start! []
  (rum/mount (Root reconciler)
             (dom/getElement "app-container"))
  )
