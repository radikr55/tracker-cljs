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
                    :message   nil
                    :offline?  false})

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
      (= 503 status) (do (citrus/dispatch! r :router :push :offline)
                         {:state {:offline? true}})
      :else          {:state {:code     400003
                              :severity "error"
                              :message  error}})))

(defmethod control :ping [_ _ state]
  {:http {:endpoint :ping
          :method   :get
          :on-load  :success-ping
          :on-error :error}})

(defmethod control :success-ping [_ _ state]
  {:state (assoc state :offline? false)})

(defmethod control :error [_ [e] state]
  (print e))
