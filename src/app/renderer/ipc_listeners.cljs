(ns app.renderer.ipc-listeners
  (:require
    [cemerick.url :refer [url]]
    [citrus.core :as citrus]
    ["electron" :refer [ipcRenderer]]))

(defn render-secret [r]
  (.on ipcRenderer "secret"
       (fn [event arg]
         (let [url-obj        (:query (url arg))
               oauth-token    (get url-obj "oauth_token")
               oauth-verifier (get url-obj "oauth_verifier")]
           (if (not= "denied" oauth-verifier)
             (citrus/dispatch! r :user :get-token {:oauth-token    oauth-token
                                                   :oauth-verifier oauth-verifier}))))))

(defn theme [r]
  (.on ipcRenderer "theme"
       (fn [event arg]
         (citrus/dispatch! r :theme :set-dark arg))))

(defn theme-default [r]
  (.on ipcRenderer "theme-default"
       (fn [event arg]
         (citrus/dispatch! r :theme :theme-default arg))))

(defn logout [r]
  (.on ipcRenderer "logout"
       (fn [event arg]
         (citrus/dispatch! r :home :open-logout))))

(defn force-logout [r]
  (.on ipcRenderer "force-logout"
       (fn [event arg]
         (citrus/dispatch! r :user :logout r))))

(defn about [r]
  (.on ipcRenderer "about"
       (fn [event arg]
         (citrus/dispatch! r :home :open-about))))

(defn refresh [r]
  (.on ipcRenderer "refresh"
       (fn [event arg]
         (citrus/dispatch! r :chart :load-track-logs))))

(defn clear-notificaiton [r]
  (.on ipcRenderer "clear-notification"
       (fn [event arg]
         (citrus/dispatch! r :chart :clear-notification))))

(defn clear-tasks [r]
  (.on ipcRenderer "clear-tasks"
       (fn [event arg]
         (citrus/dispatch! r :chart :delete-all-empty-tasks))))

(defn start! [r]
  (render-secret r)
  (refresh r)
  (theme r)
  (theme-default r)
  (logout r)
  (force-logout r)
  (about r)
  (clear-notificaiton r)
  (clear-tasks r))
