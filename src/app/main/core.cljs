(ns app.main.core
  (:require
   ["electron" :refer [app
                       BrowserWindow
                       crashReporter
                       session]]
   [app.main.auth :as auth]))

(def main-window (atom nil))

(defn init-browser []
  (reset! main-window (BrowserWindow.
                        (clj->js {:width          800
                                  :height         600
                                  :titleBarStyle  "hidden"
                                  :frame          false
                                  :webPreferences {:nodeIntegration true}})))

  (.loadURL @main-window (str "file://" js/__dirname "/public/index.html"))
  (.on @main-window "closed" #(reset! main-window nil)))

(defn main []
                                        ; CrashReporter can just be omitted
  ;; (.start crashReporter
  ;;         (clj->js
  ;;           {:companyName "MyAwesomeCompany"
  ;;            :productName "MyAwesomeApp"
  ;;            :submitURL   "https://example.com/submit-url"
  ;;            :autoSubmit  false}))

  (.on app "window-all-closed" #(when-not (= js/process.platform "darwin")
                                  (.quit app)))
  (.on app "ready" init-browser)
  )

;; (print 123)
;; (open-auth-window "http://localhost:2990/jira/plugins/servlet/oauth/authorize?oauth_token=6Z50eN8TP9zoTHxl0HJSWNKC4gD3YLHu")

;; (let [web-contents (.-webContents
;;                      @main-window                     ;; (get (.getAllWindows BrowserWindow) 0)
;;                      )]
;;   (.send web-contents "secret" "123"))
