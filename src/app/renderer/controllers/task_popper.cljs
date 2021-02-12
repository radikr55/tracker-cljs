(ns app.renderer.controllers.task-popper)

(def initial-state {:open     false
                    :code     nil
                    :position nil
                    :link     nil})

(defmulti control (fn [event] event))

(defmethod control :init []
  {:state initial-state})

(defmethod control :open-popper [_ [val] state]
  {:state (assoc state
            :position (:position val)
            :code (:code val)
            :link (:link val)
            :open true)})

(defmethod control :close-popper [_ _ state]
  {:state (assoc state
            :position nil
            :code nil
            :time nil
            :open false)})

(defmethod control :set-time [_ [time] state]
  {:state (assoc state :time time)})

