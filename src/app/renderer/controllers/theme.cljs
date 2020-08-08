(ns app.renderer.controllers.theme
  (:require
   [cljs.tools.reader.edn :as edn]
   ["@material-ui/core/styles" :refer [createMuiTheme]]))

(def dark-theme
  (createMuiTheme
    (clj->js {:palette
              (clj->js {:type      "dark"
                        :primary   {:main "#90caf9"}
                        :secondary {:main "#ff4081"}
                        :devider   "rgba(255, 255, 255, 0.12)"
                        :text      {:secondary "rgba(255, 255, 255, 0.9)"}})})))

(def light-theme
  (createMuiTheme
    (clj->js {:palette
              (clj->js {:type      "light"
                        :primary   {:main "#1976d2"}
                        :secondary {:main "#c51162"}
                        :devider   "rgba(255, 255, 255, 0.12)"
                        :text      {:secondary "rgba (0, 0, 0, 0.87)"}})})))

(defmulti control (fn [event] event))

(defmethod control :init []
  (let [from-store (edn/read-string (js/localStorage.getItem "dark"))
        theme      (if  from-store
                     dark-theme
                     light-theme)]
    {:state {:js   theme
             :cljs (js->clj theme :keywordize-keys true)}}))

(defmethod control :switch [_ _ state]
  (let [dark? (not (edn/read-string (js/localStorage.getItem "dark")))
        theme (if dark? dark-theme light-theme)]
    {:state         {:js   theme
                     :cljs (js->clj theme :keywordize-keys true)}
     :local-storage {:method :set
                     :data   dark?
                     :key    :dark}}))
