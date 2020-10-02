(ns app.main.core
  (:require
   [app.main.local-storage :as ls]
   [app.main.login.server :as login-server]
   [app.main.login.auth :as auth]
   [app.main.window :as w]
   [app.main.timer.ping :as ping]
   [app.main.timer.reduce :as r]
   ["electron" :as electron :refer [app
                                    ipcMain
                                    BrowserWindow
                                    nativeTheme
                                    crashReporter
                                    session]]))

(def icon (str js/__dirname "/public/img/icon-small.png"))

(defn init-browser []
  (reset! w/main-window (BrowserWindow.
                          (clj->js {:width          900
                                    :height         600
                                    :minWidth       900
                                    :minHeight      600
                                    :webPreferences {:nodeIntegration true}})))
  (login-server/-main)
  (w/load-local-index)
  (set! (.-themeSource nativeTheme) "dark"))

(defn main []
  (.on app "window-all-closed" #(when-not (= js/process.platform "darwin")
                                  (.quit app)))
  (.on app "ready" init-browser))

