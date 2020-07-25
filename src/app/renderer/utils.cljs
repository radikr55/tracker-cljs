(ns app.renderer.utils)

(defn rc [{:keys [el props child]}] (js/React.createElement el (clj->js props) child))
