(ns app.renderer.controllers.user)

(def initial-state {:loading? false
                    :login    nil
                    :password nil})

(defmulti control (fn [event] event))

(defmethod control :init []
  {:state initial-state})

(defmethod control :change [_ [{:keys [login password]}] state]
  ;; (assoc :articles articles)
  {:state {:login    login
           :password password}})

(defmethod control :login [event args state]
  (let [c args]
    {:state c}))
