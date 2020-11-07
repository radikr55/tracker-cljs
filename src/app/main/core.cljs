(ns app.main.core
  (:require
   [app.main.local-storage :as ls]
   [app.main.login.server :as login-server]
   [app.main.login.auth :as auth]
   [app.main.window :as w]
   [promesa.core :as p]
   [app.main.timer.ping :as ping]
   [app.main.timer.reduce :as r]
   [app.main.utils :refer [send-ipc]]
   ["electron" :as electron :refer [app
                                    ipcMain
                                    BrowserWindow
                                    nativeTheme
                                    crashReporter
                                    session]]))

(def icon (str js/__dirname "/public/img/icon-small.png"))

(defn set-theme []
  (when (.-shouldUseDarkColors nativeTheme)
    (let [web-content (.-webContents @w/main-window)]
      (->  (ls/local-get web-content "dark")
           (p/then #(when (nil? %)
                      (send-ipc @w/main-window "theme" true)
                      (set! (.-themeSource nativeTheme) "dark")))))))

(defn init-browser []
  (reset! w/main-window (BrowserWindow.
                          (clj->js {:width          900
                                    :height         600
                                    :minWidth       900
                                    :minHeight      600
                                    :webPreferences {:nodeIntegration true}})))
  (login-server/-main)
  (set-theme)
  (w/load-local-index))

(defn main []
  (.on app "window-all-closed" #(when-not (= js/process.platform "darwin")
                                  (.quit app)))
  (.on app "ready" init-browser))

