(ns app.main.core
  (:require
   ["electron" :as electron :refer [app
                                    Tray
                                    BrowserWindow
                                    crashReporter
                                    session]]
   [app.main.auth :as auth]))

(def main-window (atom nil))

;; const image = nativeImage.createFromPath('/Users/somebody/images/icon.png')

;; (.toDataURL (.createFromPath electron/nativeImage "resources/public/img/icon-small.png") )
;; (electron/Tray. )

;; (defn add-tray []
;;   (let [
;;         contexMenu (.buildFromTemplate electron/Menu
;;                                        (clj->js [{:label "test"
;;                                                   :type  "radio"}] ))
;;         tray       (Tray. "resources/public/img/icon-small.png")
;;         test       (.setContextMenu tray contexMenu)]
;;     ))

(defn init-browser []
  (reset! main-window (BrowserWindow.
                        (clj->js {:width          800
                                  :height         600
                                  :minWidth       600
                                  :minHeight      600
                                  :maxWidth       1440
                                  :titleBarStyle  "hidden"
                                  :frame          false
                                  :webPreferences {:nodeIntegration true}})))
  ;; (add-tray)
  (.loadURL @main-window (str "file://" js/__dirname "/public/index.html"))
  (.on @main-window "closed" #(reset! main-window nil))
  )

(defn main []
  ;; CrashReporter can just be omitted
  ;; (.start crashReporter
  ;;         (clj->js
  ;;           {:companyName "MyAwesomeCompany"
  ;;            :productName "MyAwesomeApp"
  ;;            :submitURL   "https://example.com/submit-url"
  ;;            :autoSubmit  false}))

  (.on app "window-all-closed" #(when-not (= js/process.platform "darwin")
                                  (.quit app)))
  (.on app "ready" init-browser))

;; (add-tray)
;; (print 123)
;; (open-auth-window "http://localhost:2990/jira/plugins/servlet/oauth/authorize?oauth_token=6Z50eN8TP9zoTHxl0HJSWNKC4gD3YLHu")

;; (let [web-contents (.-webContents
;;                      @main-window                     ;; (get (.getAllWindows BrowserWindow) 0)
;;                      )]
;;   (.send web-contents "secret" "123"))
