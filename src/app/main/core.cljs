(ns app.main.core
  (:require
   ["electron" :as electron :refer [app
                                    Tray
                                    BrowserWindow
                                    crashReporter
                                    session]]
   [app.main.auth :as auth]))

(def main-window (atom nil))

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
  )

(defn main []
  (.on app "window-all-closed" #(when-not (= js/process.platform "darwin")
                                  (.quit app)))
  (.on app "ready" init-browser))
