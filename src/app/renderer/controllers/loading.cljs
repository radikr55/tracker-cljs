(ns app.renderer.controllers.loading)

(def initial-state false)

(defmulti control (fn [event] event))

(defmethod control :init []
  {:state initial-state})

(defmethod control :on []
  {:state false})

(defmethod control :off []
  {:state true})
