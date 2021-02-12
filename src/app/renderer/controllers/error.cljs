(ns app.renderer.controllers.error
  (:require ["electron" :as electron :refer [shell remote]]
            [citrus.core :as citrus]
            [goog.string :as gstring]
            [goog.string.format]))

(def initial-state {:code      nil
                    :action    nil
                    :button    nil
                    :sevetiy   nil
                    :auto-hide false
                    :message   nil})

(defmulti control (fn [event] event))

(defmethod control :init []
  {:state initial-state})

(defmethod control :check-version [_ [error]]
  (let [version          (.getVersion (->> electron
                                           .-remote
                                           .-app))
        app-version      (js/localStorage.getItem "app-version")
        app-version-link (js/localStorage.getItem "app-version-link")]
    (if (not (= version app-version))
      {:state {:code      400001
               :severity  "warning"
               :action    #(.openExternal shell app-version-link)
               :button    "get version"
               :auto-hide true
               :message   (gstring/format "Your local version (%s) does not match the current version (%s)" version app-version)
               }}
      {:state initial-state})))

(defmethod control :show-error [_ [error r]]
  (let [status (:status error)]
    (cond
      (= 401 status) (do
                       (citrus/dispatch! r :user :logout r)
                       {:state {:code     400002
                                :severity "error"
                                :message  "Unauthorized"}})
      :else {:state {:code     400003
                     :severity "error"
                     :message  error}})))

;; (defmethod control :offline [_ [error r]]
;;   {:state {:code     400004
;;            :severity "error"
;;            :message  "Offline"
;;            :button   "Refresh"
;;            :action   #(.reload (.getCurrentWindow remote) )}})

