(ns app.renderer.controllers.home)

(def initial-state {:theme     nil
                    :left-size 100})

(defmulti control (fn [event] event))

(defmethod control :init []
  {:state initial-state})

(defmethod control :set-left-size [_ [size] state]
  {:state (assoc state :left-size (+ ( :left-size state) size))})
