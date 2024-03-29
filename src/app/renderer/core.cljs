(ns app.renderer.core
  (:require [rum.core :as rum]
            [goog.dom :as dom]
            [cljs-time.core :as t]
            ["electron-log" :as log]
            [citrus.core :as citrus]
            [app.renderer.effects :as effects]
            [app.renderer.controllers.user :as user]
            [app.renderer.controllers.chart :as chart]
            [app.renderer.controllers.router :as router]
            [app.renderer.controllers.project :as project]
            [app.renderer.controllers.loading :as loading]
            [app.renderer.controllers.theme :as theme]
            [app.renderer.controllers.chart-popper :as chart-popper]
            [app.renderer.controllers.task-popper :as task-popper]
            [app.renderer.controllers.stat-popper :as stat-popper]
            [app.renderer.controllers.calendar-popper :as calendar-popper]
            [app.renderer.controllers.home :as home]
            [app.renderer.controllers.error :as error]
            [app.renderer.forms.root :refer [Root]]
            [app.renderer.ipc-listeners :as ipc]))

(def offline-interval (atom nil))
(def offline-time 5)

(.assign js/Object js/console (.-functions log))

(enable-console-print!)

(defonce reconciler
  (citrus/reconciler
   {:state           (atom {})
    :controllers     {:loading         loading/control
                      :user            user/control
                      :router          router/control
                      :project         project/control
                      :chart           chart/control
                      :home            home/control
                      :theme           theme/control
                      :error           error/control
                      :chart-popper    chart-popper/control
                      :stat-popper     stat-popper/control
                      :calendar-popper calendar-popper/control
                      :task-popper     task-popper/control}
    :effect-handlers {:local-storage effects/local-storage
                      :ipc           effects/ipc-renderer
                      :http          effects/http}}))

(defonce init-ctrl (citrus/broadcast-sync! reconciler :init))

(defn check-not-submitted [date not-submitted]
  (effects/ipc-renderer nil nil {:type "update-clear-notification"
                                 :args (boolean (some #(t/= date %) not-submitted))}))

(defn send-ping []
  (citrus/dispatch! reconciler :error :ping)
  (js/clearTimeout @offline-interval)
  (reset! offline-interval
          (js/setTimeout send-ping (* offline-time 1000))))

(add-watch (citrus/subscription reconciler [:chart :date]) :enable-clear-notification
           (fn []
             (let [date          @(citrus/subscription reconciler [:chart :date])
                   not-submitted @(citrus/subscription reconciler [:chart :not-submitted])]
               (check-not-submitted date not-submitted))))

(add-watch (citrus/subscription reconciler [:chart :not-submitted]) :enable-clear-notification
           (fn []
             (let [date          @(citrus/subscription reconciler [:chart :date])
                   not-submitted @(citrus/subscription reconciler [:chart :not-submitted])]
               (check-not-submitted date not-submitted))))

(add-watch (citrus/subscription reconciler [:error :offline?]) :process-offline
           (fn [_ _ _ new]
             (let [start-ping #(reset! offline-interval (js/setTimeout send-ping (* offline-time 1000)))
                   stop-ping  #(do (citrus/dispatch! reconciler :router :push :home)
                                   (js/clearTimeout @offline-interval))]
               (if new
                 (start-ping)
                 (stop-ping)))))

(defn start! []
  (ipc/start! reconciler)
  (rum/mount (Root reconciler)
             (dom/getElement "app-container")))

(defn ^:dev/after-load start []
  (rum/mount (Root reconciler)
             (dom/getElement "app-container")))
