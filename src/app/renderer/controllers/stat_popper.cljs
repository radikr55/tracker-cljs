(ns app.renderer.controllers.stat-popper
  (:require [app.renderer.effects :as effects]
            [citrus.core :as citrus]
            [app.renderer.time-utils :as tu]
            [cljs-time.coerce :as c]))

(def initial-state {:open     false
                    :time     nil
                    :code     nil
                    :position nil})

(defmulti control (fn [event] event))

(defmethod control :init []
  {:state initial-state})

(defmethod control :open-popper [_ [val] state]
  {:state (assoc state
            :position (:position val)
            :code (:code val)
            :time (:time val)
            :open true)})

(defmethod control :close-popper [_ _ state]
  {:state (assoc state
            :position nil
            :code nil
            :time nil
            :open false)})

(defmethod control :set-time [_ [time] state]
  {:state (assoc state :time time)})

(defmethod control :send-time [_ [date] state]
  (let [token (effects/local-storage
                nil
                :project
                {:method :get
                 :key    :token})]
    {:http {:endpoint :submit
            :params   (assoc token :query
                                   [{:issueCode (:code state)
                                     :timeSpent (* (:time state) 60)
                                     :date      (c/to-string (tu/merge-date-time date (tu/field->to-time "12:00")))
                                     :offset    (.getTimezoneOffset (js/Date.))}])
            :method   :post
            :on-load  :success-submit
            :on-error :error}}))

(defmethod control :success-submit [event [args r] state]
  (citrus/dispatch! r :chart :load-track-logs args)
  {:state (assoc state :open false)})

(defmethod control :error [_ [error] state]
  (print error))
