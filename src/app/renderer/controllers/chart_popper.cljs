(ns app.renderer.controllers.chart-popper)

(def initial-state {:open     false
                    :start    nil
                    :end      nil
                    :position nil
                    :interval nil})

(defmulti control (fn [event] event))

(defmethod control :init []
  {:state initial-state})

(defmethod control :open-popper [_ [val] state]
  {:state (assoc state
                 :position (:position val)
                 :code (:code (:row val))
                 :start (:start (:row val))
                 :end (:end (:row val))
                 :open true)})

(defmethod control :close-popper [_ _ state]
  {:state (assoc state
                 :position nil
                 :code nil
                 :end nil
                 :start nil
                 :open false)})

(defmethod control :set-start [_ [start] state]
  (if (< start (:end state))
    {:state (assoc state :start start)}
    state))

(defmethod control :set-end [_ [end] state]
  (if (> end (:start state))
    {:state (assoc state :end end)}
    state))
