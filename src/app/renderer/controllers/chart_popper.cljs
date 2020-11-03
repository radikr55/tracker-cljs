(ns app.renderer.controllers.chart-popper
  (:require [citrus.core :as citrus]
            [cljs-time.coerce :as c]
            [app.renderer.effects :as effects]))

(def initial-state {:open         false
                    :start        nil
                    :end          nil
                    :position     nil
                    :inactive-log true
                    :interval     nil})

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
                   :inactive-log (or (nil? inactive-log) inactive-log)
                   :open true)}
    ))

(defmethod control :close-popper [_ _ state]
  {:state (assoc state
                 :position nil
                 :code nil
                 :end nil
                 :start nil
                 :inactive-log true
                 :open false)})

(defmethod control :set-start [_ [start] state]
  (if (< start (:end state))
    {:state (assoc state :start start)}
    state))

(defmethod control :set-end [_ [end] state]
  (if (> end (:start state))
    {:state (assoc state :end end)}
    state))

(defmethod control :save-time [_ [start end task] state]
  (let [token     (effects/local-storage
                    nil
                    :poject
                    {:method :get
                     :key    :token})
        offset    (.getTimezoneOffset (js/Date. start))
        inactive? (:inactive-log state)]
    {:http {:endpoint :save-ping
            :params   (assoc token
                             :data   [{:start    (c/to-string start)
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
