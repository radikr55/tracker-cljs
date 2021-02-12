(ns app.main.login.auth
  (:require ["electron" :refer [ipcMain session]]
            [goog.string :as gstring]
            [goog.string.format]
            [app.main.window :as w]
            [promesa.core :as p]))

(defn clear-cookies [url]
  (let [def-session (.-defaultSession session)
        cookies     (.-cookies def-session)
        url-obj     (js/URL. url)
        path        (gstring/format "%s//%s/jira/"
                                    (.-protocol url-obj)
                                    (.-host url-obj))]
    (.remove cookies path "JSESSIONID1")))

(defn open-auth-window [redirectUri]
  (-> (clear-cookies redirectUri)
      (p/then (fn [e]
                (.loadURL @w/main-window redirectUri)
                (.setMenu @w/main-window nil)))
      (p/catch #(print %))))

(defonce on-auth
         (.on ipcMain "oauth"
              (fn [event args]
                (open-auth-window args))))
