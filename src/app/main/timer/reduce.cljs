(ns app.main.timer.reduce
  (:require ["electron" :as electron :refer [powerMonitor]]
            [cljs-time.core :as t]
            [cljs-time.coerce :as c]
            [app.main.window :as w]
            [app.main.api :as api]
            [app.main.local-storage :as ls]
            [promesa.core :as p]
            [cljs-time.format :as ft]))

(def ls-key "time")
(def inactive-interval 5)
(def time-send (* 60 5)) ; sec
(def format "yyyy/MM/dd HH:mm")

(defn get-interval [start end]
  (t/in-minutes (t/interval start end)))

(defn set-top [coll x]
  (conj (pop coll) x))

(defn to-date [str]
  (ft/parse (ft/formatter format) str))

(defn add-minute [date min]
  (t/plus- date (t/minutes min)))

(defn interval-less? [ping-package]
  (< (get-interval (:start ping-package) (:end ping-package)) inactive-interval))

(defn merge-packages
  "Merge packages if they are in sequence (left.end + 1) = right.start"
  [left right]
  (let [left-last (last left)]
    (cond
      (nil? left-last)                                         [right]
      (and (= (:status left-last) (:status right))
           (= (add-minute (:end left-last) 1)) (:start right)) (set-top left (assoc left-last :end (:end right)))
      :else                                                    (conj left right))))

(defn set-inactive-log
  "Set inactive log boolean by status"
  [ping-package]
  (assoc ping-package :inaclive (= (:status ping-package) "active")))

(defn inactive->active
  "Switch incative status to active if less then 5"
  [ping-package]
  (if (interval-less? ping-package)
    (assoc ping-package :status "active")
    ping-package))

(defn fill-packages
  "Fill ping packages in group by active status"
  [result current]
  (let [last-res       (last result)
        current-status (:status (last current))
        current-task   (:task (last current))
        current-time   (to-date (first current))]
    (if (= (:status last-res)  current-status)
      (set-top result (assoc last-res :end current-time))
      (conj result {:start  current-time
                    :end    current-time
                    :status current-status
                    :task   current-task}))))

(defn collect-package [map-ping]
  (loop [origin map-ping
         result []]
    (if  (nil? origin)
      result
      (let [current (first origin)]
        (cond
          (empty? result) (recur (next origin) [{:start  (to-date (first current))
                                                 :end    (to-date (first current))
                                                 :status (:status (last current))
                                                 :task   (:task (last current))}])
          :else           (recur (next origin) (fill-packages result current)))))))

(defn process-ping []
  (when @w/main-window
    (let [web-content (.-webContents @w/main-window)]
      (-> (ls/local-get web-content ls-key)
          (p/then #(->> (into [] %)
                        (sort-by first)))
          (p/then (fn [ping-vector]
                    (->> (collect-package ping-vector)
                         (map #(inactive->active %))
                         (map #(set-inactive-log %))
                         (reduce #(merge-packages %1 %2) []))))
          (p/catch #(print %))))))

(defn send-ping [packages]
  (-> (api/fetch {:method   :POST
                  :data     {:data   packages
                             :token  "WZv45yWt0ReOJGAYEfJFh6e8B2nAXxrm"
                             :secret "35494zhP2SmiGGNxo774lT8HNT5YJXwZ"
                             :offset (.getTimezoneOffset (js/Date.))
                             }
                  :endpoint :save-ping})
      (p/then #(print %))
      (p/catch #(print %))))

(defonce timer-send-ping
  (js/setInterval #(send-ping process-ping) (* time-send 1000)))

;; (-> (process-ping)
;;     (p/then #(map (fn [el] (assoc el
;;                                   :start (c/to-string (:start el))
;;                                   :end (c/to-string (:end el)))) %))
;;     (p/then #(send-ping %))
;;     (p/catch #(print %)))


;; (-> (api/fetch {:method   :POST
;;                 :data     {:start  (c/to-string (t/date-time 2020 3 14))
;;                            :end    (c/to-string (t/date-time 2020 3 15))
;;                            :task   "123"}
;;                 :endpoint :save-ping})
;;     (p/then #(print %)))
