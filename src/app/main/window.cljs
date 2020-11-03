(ns app.main.window
  (:require [app.main.local-storage :as ls]
            [promesa.core :as p]
            ["electron" :as electron :refer [Menu
                                             ipcMain]]))

(def main-window (atom nil))

(defn set-dark [dark?]
  (let [web-content (.-webContents @main-window)]
    (.send web-content "theme" dark?)))

(defn logout []
  (let [web-content (.-webContents @main-window)]
    (.send web-content "logout")))

(defn menu-template [name dark?]
  (clj->js [{:label   "TaskTracker"
             :submenu [{:role "toggledevtools"}
                       {:role "reload"}
                       {:type "separator"}
                       {:label "Clear Notifications"}
                       {:label "Clear Inactive Tasks"}
                       {:type "separator"}
                       {:label   "Light theme"
                        :type    "radio"
                        :checked (not dark?)
                        :click   #(set-dark false)}
                       {:label   "Dark theme"
                        :type    "radio"
                        :checked dark?
                        :click   #(set-dark true)}
                       {:type "separator"}
                       {:label (str "Sign out " name)
                        :click logout}
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
