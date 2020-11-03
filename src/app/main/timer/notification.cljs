(ns app.main.timer.notification
  (:require ["electron" :as electron]
            [app.main.window :as w]
            [goog.string :as gstring]
            [goog.string.format]))

(def inactive-interval (atom ""))
(def icon-path (str js/__dirname "/public/img/icon@3x.png"))

(defn show [body]
  (let [notific (new electron/Notification.
                     (clj->js {:title "TaskTracker"
                               :body  body
                               :icon  icon-path}))]
    (.on notific "click" #(do (when (.isMinimized @w/main-window) (.restore @w/main-window) )
                              (.focus @w/main-window)))
    (.show notific)))

(add-watch inactive-interval :show-notify
           (fn [_ _ _ new]
             (show (gstring/format "You were not present for %s minutes" new))))

