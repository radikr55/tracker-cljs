(ns app.main.window
  (:require [app.main.local-storage :as ls]
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
    (->  (ls/local-get web-content "theme")
         (p/then #(when (= 'default %)
                    (send-ipc @main-window "theme-default" theme)
                    (reset! theme-timeout
                            (js/setTimeout timeout-theme 500)))))))

(defn set-theme []
  (reset! theme-timeout
          (js/setTimeout timeout-theme 500)))

(defn switch-theme [theme]
  (if-not (= "default" theme)
    (do (js/clearTimeout @theme-timeout)
        (send-ipc @main-window "theme" theme))
    (do (set-theme)
        (send-ipc @main-window "theme-default" theme))))

(defn menu-template [name theme]
  (clj->js [{:label   "TaskTracker"
             :submenu [{:label       "Refresh"
                        :accelerator "CmdOrCtrl+Shift+R"
                        :click       #(send-ipc @main-window "refresh" nil)}
                       {:type "separator"}
                       {:label "Clear Notifications"
                        :click #(send-ipc  @main-window "clear-notification" nil)}
                       {:label "Clear Inactive Tasks"
                        :click #(send-ipc @main-window "clear-tasks" nil)}
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

(defn set-menu []
  (let [web-content (.-webContents @main-window)]
    (-> (p/all [(ls/local-get web-content "token")
                (ls/local-get web-content "theme")])
        (p/then (fn [[token theme]]
                  (.setApplicationMenu Menu (.buildFromTemplate Menu
                                                                (menu-template (:name token) theme))))))))

(defn load-local-index []
  (.loadURL @main-window (str "file://" js/__dirname "/public/index.html"))
  (set-menu)
  (.on @main-window "closed" #(reset! main-window nil))
  (.on @main-window "close" #(when (not @force-quit)
                               (.preventDefault %)
                               (.minimize @main-window))))

(defonce on-get-name
  (.on ipcMain "update-title-bar-menu"
       #(set-menu)))

(comment
  (menu-template "rad" true))
