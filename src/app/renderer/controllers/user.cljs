(ns app.renderer.controllers.user)

(def initial-state "")

(defmulti control (fn [event] event))

(defmethod control :init []
  {:state initial-state})

(defmethod control :login [_ [x] credential]
  (do {:state x}))
