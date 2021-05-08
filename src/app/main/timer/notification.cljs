(ns app.main.timer.notification
  (:require ["electron" :as electron :refer [app
                                             ipcMain
                                             BrowserWindow
                                             ]]
            [app.main.window :as w]
            [app.main.utils :refer [send-ipc]]
            [goog.string :as gstring]
            [app.main.local-storage :as ls]
            [promesa.core :as p]
            [goog.string.format]))

(def notification-window (atom nil))
(def inactive-interval (atom ""))
(def url (str "file://" js/__dirname "/public/notification.html"))
(def icon-path (str js/__dirname "/public/img/icon@3x.png"))

(defn show [body]
  (let [notific (new electron/Notification.
                     (clj->js {:title "TaskTracker"
                               :body  body
                               :icon  icon-path}))]
    (.on notific "click" #(do (.focus @w/main-window)
                              (.restore @w/main-window)))
    (.show notific)))

(defn create-notification-window [time]
  (let [d-width (-> electron
                    .-screen
                    .getPrimaryDisplay
                    .-bounds
                    .-width)
        window  (BrowserWindow.
                  (clj->js {:width          350
                            :height         100
                            :minWidth       350
                            :minHeight      100
                            :resizable      false
                            :frame          false
                            :alwaysOnTop    true
                            :x              (- d-width 350)
                            :y              0
                            :webPreferences {:nodeIntegration    true
                                             :enableRemoteModule true}}))]
    (.loadURL window (str url "?time=" time "&theme=" @w/theme-atom))
    (.on window "blur" #(.close window))
    (reset! notification-window window)))

(add-watch inactive-interval :show-notify
           (fn [_ _ _ new]
             (when @w/main-window
               (let [web-content (.-webContents @w/main-window)]
                 (-> (ls/local-get web-content "current-task")
                     (p/then #(when (not-empty (:code %))
                                (if @w/system-notification?
                                  (show (gstring/format "You were not present for %s minutes" new))
                                  (create-notification-window new))))
                     (p/catch #(print %)))))))

(add-watch w/theme-atom :swith-theme
           (fn [_ _ _ new]
             (when @notification-window
               (send-ipc @notification-window "switch-theme" new))))

(defonce on-notification-focus
  (.on ipcMain "notification-close"
       #(do (.show @w/main-window)
            (.close @notification-window) )))

(comment
  (reset! inactive-interval 123)

  (.restore @w/main-window))

