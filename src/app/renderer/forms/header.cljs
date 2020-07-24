(ns app.renderer.forms.header
  (:require [rum.core :as rum]
            ["electron" :refer [remote]]
            [frameless-titlebar :default TitleBar]))

(def currentWindow (.getCurrentWindow remote))


(rum/defc Header [r]
  (js/React.createElement TitleBar
                          (clj->js {:title         "TaskTracker"
                                    :onMinimize    (fn [] (.minimize currentWindow))
                                    :onMaximize    (fn [] (if (.isMaximized currentWindow)
                                                            (.restore currentWindow)
                                                            (.maximize currentWindow)))
                                    :onClose       (fn [] (.close currentWindow))
                                    :onDoubleClick (fn [] (.maximize currentWindow))}))
  )
