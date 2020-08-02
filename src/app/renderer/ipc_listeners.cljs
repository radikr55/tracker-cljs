(ns app.renderer.ipc-listeners
  (:require
   [cemerick.url :refer [url]]
   [citrus.core :as citrus]
   ["electron" :refer [ipcRenderer]]))

(defn render-secret  [r]
  (.on ipcRenderer "secret"
       (fn [event arg]
         (let [url-obj        (:query (url arg))
               oauth-token    (get url-obj "oauth_token")
               oauth-verifier (get url-obj "oauth_verifier")]
           (if (not=  "denied" oauth-verifier)
             (citrus/dispatch! r :user :get-token {:oauth-token    oauth-token
                                                   :oauth-verifier oauth-verifier}))
           ))))

(defn start! [r]
  (render-secret r))


;; (.removeAllListeners ipcRenderer "secret")
;; (.on ipcRenderer "secret"
;;      #(fn [event arg]
;;         (citrus/dispatch! r :router :push {:handler :home}) ))

;; (.on ipcRenderer "secret1" #(print %2))
;; (citrus/dispatch! @reconciler :router :push {:handler :home})
