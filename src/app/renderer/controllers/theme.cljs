(ns app.renderer.controllers.theme)

(def initial-state true)

(defmulti control (fn [event] event))

(defmethod control :init []
  {:state initial-state})

(defmethod control :switch [_ _ state]
  (let [dark? (not state)]
    {:state         dark?
     :local-storage {:method :set
                     :data   dark?
                     :key    :dark
                     }}))
