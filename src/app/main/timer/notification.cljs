(ns app.main.timer.notification
  (:require ["electron" :as electron :refer [Notification]]
            [cljs-time.core :as t]
            [cljs-time.format :as ft]
            [goog.string :as gstring]
            [goog.string.format]))

(def inactive-interval (atom ""))
(def icon-path (str js/__dirname "/public/img/icon@3x.png"))

(defn show [body]
  (let [notific (new electron/Notification.
                     (clj->js {:title "TaskTracker"
                               :body  body
                               :icon  icon-path}))]
    (.show notific)))

(add-watch inactive-interval :show-notify
           (fn [_ _ old new]
             (show (gstring/format "You were not present for %s minutes" new))))

(reset! inactive-interval 1231)

;; (.-electorn (.-versions js/process ) )
