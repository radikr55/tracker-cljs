(ns app.main.window
  (:require [app.main.local-storage :as ls]
            [app.main.utils :refer [send-ipc]]
            [promesa.core :as p]
            ["electron-log" :as log]
            ["path" :as path]
            [app.main.utils :refer [send-ipc]]
            ["electron" :as electron :refer [Menu
                                             app
                                             shell
                                             nativeTheme
                                             ipcMain]]))

(def main-window (atom nil))
(def force-quit (atom nil))
(def theme-timeout (atom nil))
(def theme-atom (atom nil))
(def system-notification? (atom nil))
(def mac? (= "darwin" (.-platform js/process)))

(defn openLogs []
  (.openPath shell (.dirname path (->> log
                                       .-transports
                                       .-file
                                       .getFile
                                       str))))

(defn timeout-theme []
  (let [web-content (.-webContents @main-window)
        theme       (if (.-shouldUseDarkColors nativeTheme) "dark" "light")]
    (-> (ls/local-get web-content "theme")
        (p/then #(when (= 'default %)
                   (send-ipc @main-window "theme-default" theme)
                   (reset! theme-atom (str "theme-" theme))
                   (reset! theme-timeout
                           (js/setTimeout timeout-theme 500)))))))

(defn set-theme []
  (reset! theme-timeout
          (js/setTimeout timeout-theme 500)))

(defn switch-theme [theme]
  (if-not (= "default" theme)
    (do (js/clearTimeout @theme-timeout)
        (reset! theme-atom (str "theme-" theme))
        (send-ipc @main-window "theme" theme))
    (do (set-theme)
        (reset! theme-atom "theme-light")
        (send-ipc @main-window "theme-default" theme))))

(defn switch-notification []
  (let [web-content (.-webContents @main-window)]
    (ls/local-set web-content "system-notification?" (not @system-notification?))
    (reset! system-notification? (not @system-notification?))))

(defn menu-template [name theme notification?]
  (clj->js [{:label   "TaskTracker"
             :submenu [{:label       "Refresh"
                        :accelerator "CmdOrCtrl+Shift+R"
                        :click       #((resolve 'app.main.timer.reduce/send-fun))}
                       {:type "separator"}
                       {:label   "Clear Notifications"
                        :enabled notification?
                        :click   #(send-ipc @main-window "clear-notification" nil)}
                       {:label "Clear Inactive Tasks"
                        :click #(send-ipc @main-window "clear-tasks" nil)}
                       {:type "separator"}
                       {:label   "System notification"
                        :type    "checkbox"
                        :checked @system-notification?
                        :click   #(switch-notification)}
                       {:type "separator"}
                       {:label   "Light theme"
                        :type    "radio"
                        :checked (= 'light theme)
                        :click   #(switch-theme "light")}
                       {:label   "Dark theme"
                        :type    "radio"
                        :checked (= 'dark theme)
                        :click   #(switch-theme "dark")}
                       {:label   "System theme"
                        :type    "radio"
                        :checked (= 'default theme)
                        :click   #(switch-theme "default")}
                       {:type "separator"}
                       {:label (str "Sign out " name)
                        :click #(send-ipc @main-window "logout" nil)}
                       {:type "separator"}
                       {:label       "Quit TaskTracker"
                        :accelerator "CmdOrCtrl+Q"
                        :click       #(do
                                        (reset! force-quit true)
                                        (.quit app))}]}
            {:label   "Edit"
             :submenu (if mac? [{:role "undo"}
                                {:role "redo"}
                                {:type "separator"}
                                {:role "cut"}
                                {:role "copy"}
                                {:role "paste"}
                                {:role "delete"}]
                          [{:role "reload"}])}
            {:label   "Dev"
             :submenu [{:role "reload"}
                       {:label       "Log"
                        :accelerator "CmdOrCtrl+L"
                        :click       #(openLogs)}
                       {:label       "Switch theme"
                        :accelerator "CmdOrCtrl+T"
                        :click       #(send-ipc @main-window "theme" (if (= 'dark theme) "light" "dark"))}
                       {:role "toggledevtools"}]}]))

(defn set-menu [notification?]
  (let [web-content (.-webContents @main-window)]
    (-> (p/all [(ls/local-get web-content "token")
                (ls/local-get web-content "theme")])
        (p/then (fn [[token theme]]
                  (.setApplicationMenu Menu (.buildFromTemplate Menu
                                                                (menu-template (:name token) theme notification?))))))))

(defn init-theme []
  (-> (ls/local-get (.-webContents @main-window) "theme")
      (p/then #(if (nil? %)
                 (reset! theme-atom "theme-light")
                 (reset! theme-atom (str "theme-" %))))))

(defn init-system-notification []
  (-> (ls/local-get (.-webContents @main-window) "system-notification?")
      (p/then #(reset! system-notification? (boolean %)))))

(defn load-local-index []
  (.loadURL @main-window (str "file://" js/__dirname "/public/index.html"))
  (set-menu false)
  (init-theme)
  (init-system-notification)
  (.on app "before-quit" #(reset! force-quit true))
  (.on @main-window "closed" #(reset! main-window nil))
  (.on @main-window "close" #(when (not @force-quit)
                               (.preventDefault %)
                               (.minimize @main-window))))

(defonce on-get-name
  (.on ipcMain "update-title-bar-menu"
       #(set-menu false)))


(defonce on-get-chage-date
         (.on ipcMain "update-clear-notification"
              #(set-menu %2)))

(comment
  (menu-template "rad" true))
