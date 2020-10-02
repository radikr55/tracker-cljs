(ns app.main.timer.ping
  (:require ["electron" :as electron :refer [powerMonitor]]
            [cljs-time.core :as t]
            [app.main.window :as w]
            [app.main.local-storage :as ls]
            [promesa.core :as p]
            [cljs-time.format :as ft]))

(def time-ping 5) ; sec
(def format "yyyy/MM/dd HH:mm")
(def ls-key "time")
(def current-task "current-task")
(def status-active "active")
(def status-inactive "inactive")

(defn to-time [date]
  (ft/unparse (ft/formatter format) (t/local-date-time date)))

(defn to-date [str]
  (ft/parse (ft/formatter format) str))

(defn save-to-ls [date active-time]
  (when @w/main-window
    (let [format      (to-time date)
          web-content (.-webContents @w/main-window)
          status      (if (< active-time time-ping) status-active status-inactive)]
      (-> (ls/local-get web-content ls-key)
          (p/then
            (fn [ping] (-> (ls/local-get web-content current-task)
                           (p/then #(let [exist-status (:status (get ping format))
                                          new-status   (if (= exist-status status-active) status-active status)]
                                      (if ping
                                        (ls/local-set web-content ls-key
                                                      (assoc ping format {:status new-status
                                                                          :task   (:code %)}))
                                        (ls/local-set web-content ls-key
                                                      {format {:status status
                                                               :task   (:code %)}})))))))))))

(defonce timer-idle-time
  (js/setInterval #(save-to-ls (js/Date.)
                               (.getSystemIdleTime powerMonitor))
                  (* time-ping 1000)))

