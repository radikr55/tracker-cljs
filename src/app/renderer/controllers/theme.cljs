(ns app.renderer.controllers.theme
  (:require
   [cljs.tools.reader.edn :as edn]
   ["@material-ui/core/styles" :refer [createMuiTheme]]))

(defmulti control (fn [event] event))

(defmethod control :init []
  (let [dark? (edn/read-string (js/localStorage.getItem "dark"))
        theme (if dark? "theme-dark" "theme-light")]
    (-> js/document
        (.getElementsByTagName "html")
        first
        (.-className)
        (set! theme))
    {:state {:dark? dark?}}))

(defmethod control :set-dark [_ [dark?] state]
  (let [theme (if dark? "theme-dark" "theme-light")]
    (-> js/document
        (.getElementsByTagName "html")
        first
        (.-className)
        (set! theme))
    {:state         {:dark? dark?}
     :ipc           {:type "update-title-bar-menu"}
     :local-storage {:method :set
                     :data   dark?
                     :key    :dark}}))

