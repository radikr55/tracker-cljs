(ns app.renderer.controllers.calendar-popper
  (:require [app.renderer.time-utils :as tu]
            [app.renderer.effects :as effects]
            [citrus.core :as citrus]
            [cljs-time.core :as t]))

(def initial-state {:open?       false
                    :date        nil
                    :stat        nil
                    :select-week nil
                    :position    nil})

(defmulti control (fn [event] event))

(defmethod control :init []
  {:state initial-state})

(defmethod control :open-popper [_ [val] state]
  {:state (assoc state
            :position (:position val)
            :date (:date val)
            :select-week (t/week-number-of-year (:date val))
            :open? true)})

(defmethod control :close-popper [_ _ state]
  {:state (assoc state
            :position nil
            :open? false)})

(defmethod control :set-select-week [_ [week] state]
  {:state (assoc state :select-week week)})

(defmethod control :previously-month [_ [r] state]
  (let [date (:date state)]
    (citrus/dispatch! r :calendar-popper :load-stat)
    {:state (assoc state :date (t/minus- date (t/months 1)))}))

(defmethod control :next-month [_ [r] state]
  (let [date (:date state)]
    (citrus/dispatch! r :calendar-popper :load-stat)
    {:state (assoc state :date (t/plus- date (t/months 1)))}))

(defmethod control :load-stat [_ _ state]
  (let [token    (effects/local-storage
                   nil
                   :calendar_popper
                   {:method :get
                    :key    :token})
        date-str (tu/date->calendar-date (:date state))]
    {:http {:endpoint :load-stat
            :params   (assoc token :query {:key date-str})
            :method   :post
            :on-load  :success-load-stat
            :on-error :error}}))

(defmethod control :success-load-stat [_ [args] state]
  {:state (assoc state :stat args)})

(defmethod control :error [_ [error] _]
  (print error))
