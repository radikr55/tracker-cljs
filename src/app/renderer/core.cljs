(ns app.renderer.core
  (:require [rum.core :as rum]
            ["electron" :refer [remote]]
            [goog.dom :as dom]
            [citrus.core :as citrus]
            [app.renderer.effects :as effects]
            ;; [app.renderer.controllers.user :as user]
            [app.renderer.controllers.user-mock :as user]
            [app.renderer.controllers.chart :as chart]
            [app.renderer.controllers.router :as router]
            [app.renderer.controllers.project :as project]
            [app.renderer.controllers.loading :as loading]
            [app.renderer.controllers.theme :as theme]
            [app.renderer.controllers.home :as home]
            [app.renderer.forms.root :refer [Root]]
            [app.renderer.ipc-listeners :as ipc]))

(enable-console-print!)

(defonce reconciler
  (citrus/reconciler
    {:state           (atom {})
     :controllers     {:loading loading/control
                       :user    user/control
                       :router  router/control
                       :project project/control
                       :chart   chart/control
                       :home    home/control
                       :theme   theme/control}
     :effect-handlers {:local-storage effects/local-storage
                       :ipc           effects/ipc-renderer
                       :http          effects/http}}))

(defonce init-ctrl (citrus/broadcast-sync! reconciler :init))

;; (citrus/dispatch! reconciler :router :push :home)
;; (citrus/dispatch! reconciler :home :plus-line true)

;; (add-watch refresh-atom
;;            :watcher
;;            #(rum/mount (Root reconciler)
;;                        (dom/getElement "app-container")))

(defn start! []
  (ipc/start! reconciler)
  (rum/mount (Root reconciler)
             (dom/getElement "app-container")))

(defn ^:dev/after-load start []
  (rum/mount (Root reconciler)
             (dom/getElement "app-container")))


