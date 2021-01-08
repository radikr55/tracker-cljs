(ns app.main.timer.notification
  (:require ["electron" :as electron]
            [app.main.window :as w]
            [goog.string :as gstring]
            [app.main.local-storage :as ls]
            [promesa.core :as p]
            [goog.string.format]))

(def inactive-interval (atom ""))
(def icon-path (str js/__dirname "/public/img/icon@3x.png"))

(defn show [body]
  (let [notific (new electron/Notification.
                     (clj->js {:title "TaskTracker"
                               :body  body
                               :icon  icon-path}))]
    (.on notific "click" #(do (.focus  @w/main-window)
                              (.restore @w/main-window)))
    (.show notific)))

(add-watch inactive-interval :show-notify
           (fn [_ _ _ new]
             (when @w/main-window
               (let [web-content (.-webContents @w/main-window)]
                 (-> (ls/local-get web-content "current-task")
                     (p/then #(when (not-empty (:code %))
                                (show (gstring/format "You were not present for %s minutes" new))))
                     (p/catch #(print %)))))))

(comment
  (reset! inactive-interval 123)

  (.restore @w/main-window)
  )

