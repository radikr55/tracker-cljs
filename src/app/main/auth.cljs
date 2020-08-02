(ns app.main.auth
  (:require ["electron" :refer [BrowserWindow ipcMain session]]
            [goog.string :as gstring]
            [goog.string.format]
            [promesa.core :as p]))

(def auth-window (atom nil))
(def eve (atom nil))

(defn clear-cookies [url]
  (let [def-session (.-defaultSession session)
        cookies     (.-cookies def-session)
        url-obj     (js/URL. url)
        path        (gstring/format "%s//%s/jira/"
                                    (.-protocol url-obj)
                                    (.-host url-obj))]
    (.remove cookies path "JSESSIONID")))

(defn open-auth-window [redirectUri]
  (reset! auth-window  (BrowserWindow.
                         (clj->js {:width          800
                                   :height         600
                                   :modal          true
                                   :webPreferences {:nodeIntegration true}})))
  (let [web-contents (.-webContents @auth-window)]
    (-> (clear-cookies redirectUri)
        (p/then (fn [e]
                  (.loadURL @auth-window redirectUri)
                  (.setMenu @auth-window nil)
                  (.show @auth-window)
                  ;; (.on @auth-window "closed" #(reset! auth-window nil))
                  (.on web-contents "will-redirect"
                       (fn [event url]
                         (.preventDefault event)
                         (.reply @eve "secret" url)
                         (.close @auth-window)))))
        (p/catch #(print %)))))

(defonce on-auth
  (.on ipcMain "oauth"
       (fn [event args]
         (reset! eve event)
         (open-auth-window args))))


