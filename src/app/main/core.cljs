(ns app.main.core
  (:require ["electron-log" :as log]
            [app.main.local-storage :as ls]
            [app.main.login.server :as login-server]
            [app.main.login.auth :as auth]
            [app.main.window :as w]
            [promesa.core :as p]
            [app.main.timer.ping :as ping]
            [app.main.timer.reduce :as r]
            [app.main.utils :refer [send-ipc]]
            [promesa.core :as p]
            ["path" :as path]
            ["os" :as os]
            ["electron" :as electron :refer [app
                                             session
                                             Menu
                                             BrowserWindow
                                             globalShortcut
                                             nativeTheme]]))

(.assign js/Object js/console (.-functions log))

(def icon (str js/__dirname "/public/img/icon.png"))

(if (not (.requestSingleInstanceLock app))
  (.quit app)
  (.on app "second-instance" #(when @w/main-window
                                (.restore @w/main-window)
                                (.focus @w/main-window))))

(defn add-shortcuts []
  (.register globalShortcut "CommandOrControl+H" #(send-ipc @w/main-window "about" nil)))

;(def react-dev-tools-path (.join path (.homedir os)
;                                 "/Library/Application Support/Google/Chrome/Profile 1/Extensions/fmkadmapgofadopljbjfkapdkoienihi/4.10.1_0"))
;
;(-> (.whenReady app)
;    (p/then #(doto (.-defaultSession session)
;               (.loadExtension react-dev-tools-path (clj->js {:allowFileAccess true})))))

(defn init-browser []
  (reset! w/main-window (BrowserWindow.
                          (clj->js {:width          900
                                    :height         600
                                    :minWidth       900
                                    :minHeight      600
                                    :webPreferences {:nodeIntegration    true
                                                     :enableRemoteModule true}})))
  (login-server/-main)
  (w/set-theme)
  (add-shortcuts)
  (w/load-local-index)
  (.setApplicationMenu Menu (.buildFromTemplate Menu
                                                (w/menu-template "" 'default false))))

(defn main []
  (.on app "window-all-closed" #(.quit app))
  (.on app "ready" init-browser))

