(ns app.renderer.controllers.home)

(def initial-state {:theme            nil
                    :chart-ref        nil
                    :position-submenu {:mouseX nil
                                       :mouseY nil}
                    :submenu-code     nil
                    :middle-list-ref  nil
                    :left-list-ref    nil
                    :right-list-ref   nil
                    :mouse-resize     false})

(defmulti control (fn [event] event))

(defmethod control :init []
  {:state initial-state})

(defmethod control :close-submenu [_ [val] state]
  {:state (assoc state
                 :position-submenu (:position-submenu initial-state)
                 :submenu-code nil)})

(defmethod control :open-submenu [_ [val] state]
  {:state (assoc state
                 :position-submenu (:position-submenu val)
                 :submenu-code (:submenu-code val))})

(defmethod control :set-chart-ref [_ [val] state]
  {:state (assoc state :chart-ref val)})

(defmethod control :set-middle-list-ref [_ [val] state]
  {:state (assoc state :middle-list-ref val)})

(defmethod control :set-right-list-ref [_ [val] state]
  {:state (assoc state :right-list-ref val)})

(defmethod control :set-left-list-ref [_ [val] state]
  {:state (assoc state :left-list-ref val)})

(defmethod control :set-chart-position [_ [position] state]
  (let [pos (+ (:chart-position state) position)]
    (when (and (<=  pos (:max-chart-scroll state)) (>= pos 0))
      {:state (assoc state :chart-position pos)})))
