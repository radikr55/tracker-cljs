(ns app.renderer.controllers.theme
  (:require
   [cljs.tools.reader.edn :as edn]
   ["@material-ui/core/styles" :refer [createMuiTheme]]))


(defmulti control (fn [event] event))

(defmethod control :init []
  {:state {}})

(defmethod control :switch [_ _ state]
  (let [dark? (not (edn/read-string (js/localStorage.getItem "dark")))
        theme (if dark? "theme-dark" "theme-light")]
    (-> js/document
        (.getElementsByTagName "html")
        first
        (.-className)
        (set! theme))
    {:local-storage {:method :set
                     :data   dark?
                     :key    :dark}}))
