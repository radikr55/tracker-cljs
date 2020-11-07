(ns app.main.window
  (:require [app.main.local-storage :as ls]
            [promesa.core :as p]
            [app.main.utils :refer [send-ipc]]
            ["electron" :as electron :refer [Menu
                                             ipcMain]]))

(def main-window (atom nil))

(defn menu-template [name dark?]
  (clj->js [{:label   "TaskTracker"
             :submenu [{:role "toggledevtools"}
                       {:role "reload"}
                       {:label       "Refresh"
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
                        :checked (not dark?)
                        :click   #(send-ipc @main-window "theme" false)}
                       {:label   "Dark theme"
                        :type    "radio"
                        :checked dark?
                        :click   #(send-ipc @main-window "theme" true)}
                       {:type "separator"}
                       {:label (str "Sign out " name)
                        :click #(send-ipc @main-window "logout" nil)}
                       {:type "separator"}
                       {:label "Quit TaskTracker"
                        :role  "quit"}]}]))

(defn set-menu []
  (let [web-content (.-webContents @main-window)]
    (-> (p/all [(ls/local-get web-content "token")
                (ls/local-get web-content "dark")])
        (p/then (fn [[token dark?]]
                  (.setApplicationMenu Menu (.buildFromTemplate Menu
                                                                (menu-template (:name token) dark?))))))))

(defn load-local-index []
  (.loadURL @main-window (str "file://" js/__dirname "/public/index.html"))
  (set-menu)
  (.on @main-window "closed" #(reset! main-window nil)))

(defonce on-get-name
  (.on ipcMain "update-title-bar-menu"
       #(set-menu)))
