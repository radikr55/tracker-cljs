(ns app.renderer.controllers.user
  (:require
   [citrus.core :as citrus]
   [goog.string :as gstring]
   [goog.string.format]))

(def initial-state {:login    nil
                    :token    nil
                    :password nil
                    :error    nil})

(defmulti control (fn [event] event))

(defmethod control :init []
  {:state initial-state})

(defmethod control :login [event [form] state]
  {:state (merge state form)
   :http  {:endpoint :login
           :params   form
           :method   :post
           :on-load  :success-login
           :on-error :error-login}})

(defmethod control :logout [event [reconciler] state]
  (citrus/dispatch! reconciler :router :push {:handler :login})
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
             :on-error :error}}))

(defmethod control :success-oauth [event [result reconciler] state]
  (let [token  (:oauth_token (:body result))
        secret (:oauth_token_secret (:body result))]
    (citrus/dispatch! reconciler :router :push {:handler :home})
    {:state         (assoc state :token token :error nil)
     :local-storage {:method :set
                     :data   {:token  token
                              :secret secret}
                     :key    :token}}))

(defmethod control :init-token [_ [token] state]
  {:state (assoc state :token token)})

(defmethod control :success-login [event args state]
  (let [url       (first args)
        with-cred (gstring/format "%s&os_username=%s&os_password=%s"
                                  (:url url)
                                  (js/encodeURIComponent (:login state))
                                  (js/encodeURIComponent (:password state)))]
    {:ipc {:type "oauth"
           :args with-cred}}))

(defmethod control :error-login [_ [error] state]
  {:state (assoc  state :error (boolean error))})

(defmethod control :error-clean [_ _ state]
  {:state (dissoc state :error)})

(defmethod control :error [_ [error] state]
  (print error))
