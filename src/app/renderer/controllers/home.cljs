(ns app.renderer.controllers.home)

(def initial-state {:theme           nil
                    :show-plus-line? false
                    :mouse-time      nil
                    :mouse-position  nil
                    :ref-timeline    (js/React.createRef)
                    :ref-chart       (js/React.createRef)
                    :statistic       #{:logged
                                       :tracked
                                       :submitted}})

(defmulti control (fn [event] event))

(defmethod control :init []
  {:state initial-state})

(defmethod control :switch-board [_ [board-key] state]
  (let [statistic (:statistic state)]
    (if (contains? statistic board-key)
      (when (> (count statistic) 1) {:state (assoc state :statistic (disj statistic board-key))} )
      {:state (assoc state :statistic (conj statistic board-key))})))

(defmethod control :set-time [_ [time] state]
  {:state (assoc state :mouse-time time)})

(defmethod control :set-mouse-position [_ [position] state]
  {:state (assoc state :mouse-position position)})

(defmethod control :show-plus-line [_ [show] state]
  {:state (assoc state :show-plus-line? show)})
