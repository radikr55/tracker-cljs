(ns app.renderer.controllers.user
  (:require [citrus.core :as citrus]
            [app.renderer.effects :as effects]))

(def initial-state {:login    nil
                    :token    nil
                    :password nil})

(defmulti control (fn [event] event))

(defmethod control :init []
  {:state initial-state})

(defmethod control :get-link [event [_] state]
  {:http {:endpoint :auth-link
          :method   :get
          :on-load  :success-get-link
          :on-error :error-login}})

(defmethod control :logout [event [reconciler] state]
  (effects/local-storage
    nil
    :project
    {:method :remove
     :key    :time})
  (effects/local-storage
    nil
    :project
    {:method :remove
     :key    :current-task})
  (citrus/dispatch! reconciler :router :push :login)
  {:state         (dissoc state :token)
   :local-storage {:method :remove
                   :key    :token}})

(defmethod control :get-token [_ args state]
  (let [tokens (first args)]
    {:state (merge state tokens)
     :http  {:endpoint :oauth
             :params   tokens
             :method   :post
             :on-load  :success-oauth
             :on-error :error-oauth}}))

(defmethod control :error-oauth [event [result reconciler] state]
  (citrus/dispatch! reconciler :router :push :login))

(defmethod control :success-oauth [event [result reconciler] state]
  (let [token  (-> result :body :oauth_token)
        secret (-> result :body :oauth_token_secret)]
    (citrus/dispatch! reconciler :router :push :home)
    {:state         (assoc state :token token)
     :http          {:endpoint :user-name
                     :params   {:token  token
                                :secret secret}
                     :method   :post
                     :on-load  :success-get-name
                     :on-error :error}
     :local-storage {:method :set
                     :data   {:token  token
                              :secret secret}
                     :key    :token}}))

(defmethod control :success-get-name [event [result reconciler] state]
  (let [user-name (-> result :name)]
    {:ipc           {:type "update-title-bar-menu"}
     :local-storage {:method :add
                     :data   {:name user-name}
                     :key    :token}
     :state         state}))

(defmethod control :init-token [_ [token] state]
  {:state (assoc state :token token)})

(defmethod control :success-get-link [event [args] state]
  (let [link (:url args)]
    {:ipc   {:type "oauth"
             :args link}
     :state state}))

(defmethod control :error [_ [error] state]
  (print error))
