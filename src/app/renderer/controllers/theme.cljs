(ns app.renderer.controllers.theme
  (:require
    [cljs.tools.reader.edn :as edn]))

(defmulti control (fn [event] event))

(defmethod control :init []
  (let [dark? (edn/read-string (js/localStorage.getItem "theme"))
        theme (if (= 'dark dark?) "theme-dark" "theme-light")]
    (-> js/document
        (.getElementsByTagName "html")
        first
        (.-className)
        (set! theme))
    {:state {:dark? (= 'dark dark?)}}))

(defmethod control :set-dark [_ [dark?] state]
  (let [theme (if (= "dark" dark?) "theme-dark" "theme-light")]
    (-> js/document
        (.getElementsByTagName "html")
        first
        (.-className)
        (set! theme))
    {:state         {:dark? (= "dark" dark?)}
     :ipc           {:type "update-title-bar-menu"}
     :local-storage {:method :set
                     :data   dark?
                     :key    :theme}}))

(defmethod control :theme-default [_ [theme] state]
  (if (= "default" theme)
    {:local-storage {:method :set
                     :data   "default"
                     :key    :theme}}
    (let [theme-css (if (= "dark" theme) "theme-dark" "theme-light")]
      (-> js/document
          (.getElementsByTagName "html")
          first
          (.-className)
          (set! theme-css))
      {:state         {:dark? (= "dark" theme)}
       :local-storage {:method :set
                       :data   "default"
                       :key    :theme}})))
