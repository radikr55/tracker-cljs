(ns app.renderer.controllers.home)

(def initial-state {:theme           nil
                    :show-plus-line? false
                    :mouse-time      nil
                    :mouse-position  nil})


(defmulti control (fn [event] event))

(defmethod control :init []
  {:state initial-state})

(defmethod control :set-time [_ [time] state]
  {:state (assoc state :mouse-time time)})

(defmethod control :set-mouse-position [_ [position] state]
  {:state (assoc state :mouse-position position)})

(defmethod control :plus-line [_ [show] state]
  {:state (assoc state :show-plus-line? show)})

(defmethod control :set-theme [_ [theme] state]
  {:state (assoc state :theme (:cljs theme ))})
