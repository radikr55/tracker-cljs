(ns app.renderer.controllers.home)

(def initial-state {
                    :search-dialog false
                    :theme         nil })

(defmulti control (fn [event] event))

(defmethod control :init []
  {:state initial-state})

(defmethod control :open-dialog [_ _ state]
  {:state (assoc state :search-dialog true)})

(defmethod control :close-dialog [_ _ state]
  {:state (assoc state :search-dialog false)})

(defmethod control :set-theme [_ [theme] state]
  {:state (assoc state :theme (js->clj theme :keywordize-keys true))})
