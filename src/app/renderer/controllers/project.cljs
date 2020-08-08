(ns app.renderer.controllers.project
  (:require [app.renderer.effects :as effects]))

(def initial-state {})

(defmulti control (fn [event] event))

(defmethod control :init [_ [reconciler]]
  {:state initial-state}
  ;; (let [token (effects/local-storage
  ;;               reconciler :project
  ;;               {:method :get
  ;;                :key    :token})]
  ;;   {:state initial-state
  ;;    :http  {:endpoint :project
  ;;            :params   token
  ;;            :method   :post
  ;;            :on-load  :success
  ;;            :on-error :error}})
  )

(defmethod control :get [_ [reconciler]]
  (let [token (effects/local-storage
                reconciler :project
                {:method :get
                 :key    :token})]
    {:http {:endpoint :project
            :params   token
            :method   :post
            :on-load  :success
            :on-error :error}}))

(defmethod control :success [event [args] state]
  {:state (js->clj
            (.parse js/JSON (:body args))
            :keywordize-keys true)})

(defmethod control :error [_ [error] state]
  (print error))
