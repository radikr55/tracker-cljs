(ns app.main.core
  (:require
   ["electron" :as electron :refer [app
                                    ipcMain
                                    Tray
                                    Menu
                                    BrowserWindow
                                    nativeTheme
                                    crashReporter
                                    session]]
   [app.main.auth :as auth]))

(def main-window (atom nil))
(def menu-template (clj->js [{:label   "TaskTracker"
                              :submenu [{:role "toggledevtools"}
                                        {:role "reload"}
                                        {:role "quit"}]}]))

(defn init-browser []
  (reset! main-window (BrowserWindow.
                        (clj->js {:width          800
                                  :height         600
                                  :minWidth       600
                                  :minHeight      600
                                  :maxWidth       1440
                                  :webPreferences {:nodeIntegration true}})))
  (.loadURL @main-window (str "file://" js/__dirname "/public/index.html"))
  (.on @main-window "closed" #(reset! main-window nil))
  (.setApplicationMenu Menu (.buildFromTemplate Menu menu-template)))

(defn main []
  (.on app "window-all-closed" #(when-not (= js/process.platform "darwin")
                                  (.quit app)))
  (.on app "ready" init-browser))
