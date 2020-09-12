(ns app.renderer.controllers.home)

(def initial-state {:chart-ref        nil
                    :position-submenu {:mouseX nil
                                       :mouseY nil}
                    :open-popover     false
                    :open-left        false
                    :row-box          nil
                    :submenu-code     nil
                    :middle-list-ref  nil
                    :left-list-ref    nil
                    :right-list-ref   nil
                    :scale            2
                    :mouse-resize     false})

(defmulti control (fn [event] event))

(defmethod control :init []
  {:state initial-state})

(defmethod control :close-submenu [_ [val] state]
  {:state (assoc state
                 :position-submenu (:position-submenu initial-state)
                 :open-left    false
                 :submenu-code nil)})

(defmethod control :open-submenu [_ [val] state]
  {:state (assoc state
                 :position-submenu (:position-submenu val)
                 :open-left  true
                 :submenu-code (:submenu-code val))})

(defmethod control :close-chart-menu [_ [val] state]
  {:state (assoc state
                 :position-submenu (:position-submenu initial-state)
                 :row-box nil
                 :open-popover false)})

(defmethod control :open-chart-menu [_ [val] state]
  {:state (assoc state
                 :position-submenu (:position-submenu val)
                 :row-box (:row-box val)
                 :open-popover true)})

(defmethod control :set-chart-ref [_ [val] state]
  {:state (assoc state :chart-ref val)})

(defmethod control :set-middle-list-ref [_ [val] state]
  {:state (assoc state :middle-list-ref val)})

(defmethod control :set-right-list-ref [_ [val] state]
  {:state (assoc state :right-list-ref val)})

(defmethod control :set-left-list-ref [_ [val] state]
  {:state (assoc state :left-list-ref val)})

(defmethod control :inc-scale [_ _ state]
  {:state (assoc state :scale (inc (:scale state)))})

(defmethod control :dec-scale [_ _ state]
  (let [scale (dec (:scale state))]
    (when (>= scale 1)
      {:state (assoc state :scale scale)})))

(defmethod control :set-chart-position [_ [position] state]
  (let [pos (+ (:chart-position state) position)]
    (when (and (<=  pos (:max-chart-scroll state)) (>= pos 0))
      {:state (assoc state :chart-position pos)})))
