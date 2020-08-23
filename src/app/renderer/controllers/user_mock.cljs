(ns app.renderer.controllers.user-mock
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

(defmethod control :login [event [form reconciler] state]
  (let [login    (:login form)
        password (:password form)]
    (if (and (= password "admin") (= login "admin"))
      (do
        (citrus/dispatch! reconciler :router :push :home)
        {:state         (assoc state :login login :password password :token "test")
         :local-storage {:method :set
                         :data "test"
                         :key    :token}} )
      {:state (assoc state :error true)}))
  )

(defmethod control :logout [event [reconciler] state]
  (citrus/dispatch! reconciler :router :push :login)
  {:state         (dissoc state :token)
   :local-storage {:method :remove
                   :key    :token}})

(defmethod control :init-token [_ [token] state]
  {:state (assoc state :token token)})

(defmethod control :error-login [_ [error] state]
  {:state (assoc  state :error (boolean error))})

(defmethod control :error-clean [_ _ state]
  {:state (dissoc state :error)})

(defmethod control :error [_ [error] state]
  (print error))
