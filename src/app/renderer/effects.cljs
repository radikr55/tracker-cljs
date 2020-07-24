(ns app.renderer.effects
  (:require [citrus.core :as citrus]
            [app.renderer.api :as api]
            [promesa.core :as p]))

(defn local-storage [reconciler controller-name effect]
  (let [{:keys [method data key on-read]} effect]
    (case method
      :set (js/localStorage.setItem (name key) data)
      :get (->> (js/localStorage.getItem (name key))
                (citrus/dispatch! reconciler controller-name on-read))
      nil)))
