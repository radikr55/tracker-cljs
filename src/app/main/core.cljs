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
                                    Menu
                                    BrowserWindow
                                    nativeTheme]]))

(def icon (str js/__dirname "/public/img/icon.png"))

(if (not (.requestSingleInstanceLock app))
  (.quit app)
  (.on app "second-instance" #(when @w/main-window
                                (.restore @w/main-window)
                                (.focus @w/main-window))))

(defn set-theme []
  (when (.-shouldUseDarkColors nativeTheme)
    (let [web-content (.-webContents @w/main-window)]
      (->  (ls/local-get web-content "dark")
           (p/then #(when (nil? %)
                      (send-ipc @w/main-window "theme" true)
                      (set! (.-themeSource nativeTheme) "dark")))))))

(defn set-events []
  ;; (.on @w/main-window "close" #(do (.preventDefault %)
  ;;                                  (.minimize @w/main-window)))
  )

(defn init-browser []
  (reset! w/main-window (BrowserWindow.
                          (clj->js {:width          900
                                    :height         600
                                    :minWidth       900
                                    :minHeight      600
                                    :webPreferences {:nodeIntegration true}})))
  (login-server/-main)
  (set-events)
  (set-theme)
  (w/load-local-index)
  (.setApplicationMenu Menu (.buildFromTemplate Menu
                                                (w/menu-template "" false))))

(defn main []
  (.on app "window-all-closed" #(when-not (= js/process.platform "darwin")
                                  (.quit app)))
  (.on app "ready" init-browser))

