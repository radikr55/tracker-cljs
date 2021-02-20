(ns app.renderer.controllers.chart-popper
  (:require [citrus.core :as citrus]
            [cljs-time.coerce :as c]
            [app.renderer.time-utils :as tu]
            [cljs-time.core :as t]
            [app.renderer.effects :as effects]))

(def initial-state {:open     false
                    :start    nil
                    :end      nil
                    :position nil
                    :disabled true})

(defmulti control (fn [event] event))

(defmethod control :init []
  {:state initial-state})

(defmethod control :open-popper [_ [val] state]
  (let [row          (:row val)
        inactive-log (:inactive-log row)]
    {:state (assoc state
              :position (:position val)
              :code (:code row)
              :start (:start row)
              :end (:end row)
              :max-start (:max-start row)
              :min-start (:min-start row)
              :max-end (:max-end row)
              :min-end (:min-end row)
              :open true
              :disabled (= (:start row) (:end row)))}))

(defmethod control :close-popper [_ _ state]
  {:state (assoc state
            :position nil
            :code nil
            :end nil
            :start nil
            :max-start nil
            :min-start nil
            :max-end nil
            :min-end nil
            :open false)})

(defmethod control :set-start [_ [start] state]
  (let [{max-start :max-start
         min-start :min-start} state
        max-bool (not (boolean max-start))
        min-bool (not (boolean min-start))]
    (if (and (< start (:end state))
             (or min-bool (>= start min-start))
             (or max-bool (<= start max-start)))
      {:state (assoc state :start start
                           :disabled false)}
      {:state (assoc state :disabled true)}))
  )

(defmethod control :set-end [_ [end] state]
  (let [{max-end :max-end
         min-end :min-end} state
        max-bool (not (boolean max-end))
        min-bool (not (boolean min-end))]
    (if (and (t/after? (tu/get-local-without-offset (t/now)) end)
             (t/after? end (:start state))
             (or min-bool (>= end min-end))
             (or max-bool (<= end max-end)))
      {:state (assoc state :end end
                           :disabled false)}
      {:state (assoc state :disabled true)})))

(defmethod control :save-time [_ [start end task] state]
  (let [token  (effects/local-storage
                 nil
                 :project
                 {:method :get
                  :key    :token})
        offset (.getTimezoneOffset (js/Date. start))]
    {:http {:endpoint :save-ping
            :params   (assoc token
                        :data [{:start    (c/to-string start)
                                :end      (c/to-string end)
                                :status   "inactive"
                                :task     task
                                :inactive true}]
                        :offset offset)
            :method   :post
            :on-load  :success-save
            :on-error :error}}))

(defmethod control :success-save [event [args r] state]
  (citrus/dispatch! r :chart :load-track-logs args)
  {:state (assoc state :open false)})

(defmethod control :error [_ [error] state]
  (print error))
