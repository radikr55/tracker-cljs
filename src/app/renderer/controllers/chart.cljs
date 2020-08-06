(ns app.renderer.controllers.chart
  (:require [cljs-time.core :as t]))

(defn get-interval [start end]
  (t/in-minutes (t/interval start end)))

(def initial-state [
                    ;; {:start     (t/today-at 7 00)
                    ;;  :end       (t/today-at 8 00)
                    ;;  :task-code "TEST 1"}
                    ;; {:start     (t/today-at 10 00)
                    ;;  :end       (t/today-at 11 00)
                    ;;  :task-code "TEST 1"}
                    {:start     (t/today-at 12 00)
                     :end       (t/today-at 13 00)
                     :task-code "TEST 1"}
                    {:start     (t/today-at 13 00)
                     :end       (t/today-at 14 00)
                     :task-code "TEST 2"}])

(defmulti control (fn [event] event))

(defn add-stubs [origin]
  (let [start     (:start (first origin))
        end       (:end (last origin))
        start-day (t/at-midnight start)
        end-day   (t/at-midnight (t/plus- end (t/days 1))) ]
    (cond-> origin
      (t/after? start start-day) (->> (concat [{:start start-day
                                                :end   start}]))
      (t/before? end end-day)    (concat [{:start end
                                           :end   end-day}]))))
(defn calc-interval [origin]
  (mapv (fn [origin]
          (let [{:keys [start end]} origin]
            (assoc origin :interval
                   (get-interval start end))))
        origin))

(defmethod control :init []
  (let [state (->> initial-state
                   add-stubs
                   calc-interval)]
    {:state state}))

(defmethod control :load [_ _ state]
  {:state state})

;; (defn merge-date [date time]
;;   (t/date-time))
;; (t/before? (t/date-time 1986 10 14 23 59) (t/today))

;; (t/in-minutes (t/date-time 1986 10 14 23 59))
;; (t/before? (t/date-time 1986 10 14 0 0) (t/date-time 1986 10 14 0 1))
;; (t/at-midnight (t/today-at 10 0))
;; (reduce #(+ %1 (:interval %2))  0 (->> initial-state
;;                                        add-stubs
;;                                        calc-interval
;;                                        ))

;; (when (= 1 1) true)
