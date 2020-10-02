(ns app.main.login.server
  (:require [app.main.window :as w]
            ["express" :as express]))

(def port 8666)

(defonce app (express))

(def server-atom (atom nil))

(.get app "/login"
      (fn [req res]
        (w/load-local-index)
        (let [web-content (.-webContents @w/main-window)]
          (.send res "!")
          (.once web-content "dom-ready"
                 #(.send web-content "secret" (.-originalUrl req))))))

(defn -main [& mess]
  (let [server (.listen app port
                        #(println "Server running at http://127.0.0.1:8666/"))]
    (reset! server-atom server)))

(comment
  (-main)
  (.close @server-atom))
