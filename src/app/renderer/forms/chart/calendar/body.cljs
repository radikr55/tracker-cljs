(ns app.renderer.forms.chart.calendar.body
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [app.renderer.utils :refer [tc]]
            [cljs-time.core :as t]
            [app.renderer.time-utils :as tu]))

(def day-names ["MO" "TU" "WE" "TH" "FR" "SA" "SU"])

(rum/defc stub-day
  [dates]
  (let [size (count dates)]
    (map #(identity [:div {:class "calendar-day"
                           :key   (str %1 "stub-date")}])
         (range (- 7 size)))))

(rum/defc day < rum/reactive
  {:key-fn (fn [_ index] (str "date" index))}
  [r index date]
  (let [chart-date (rum/react (citrus/subscription r [:chart :date]))
        class      (cond-> "calendar-day calendar-actual-day "
                     (t/= chart-date date) (str " calendar-selected " ))]
    [:div {:class   class
           :onClick #(do (citrus/dispatch! r :chart :set-date date)
                         (citrus/dispatch! r :calendar-popper :close-popper)
                         (citrus/dispatch! r :chart :load-track-logs))}
     (t/day date)] ))

(rum/defc weeks < rum/reactive
  {:key-fn (fn [_ index] (str "week" index))}
  [r index [week dates] first?]
  (let [select-week (rum/react (citrus/subscription r [:calendar-popper :select-week]))
        class       (cond-> "calendar-week "
                      (= week select-week) (str " calendar-select-week"))]
    [:div {:class       class
           :onMouseOver #(citrus/dispatch! r :calendar-popper :set-select-week week)}
     [(when first? (stub-day dates))
      (map-indexed #(day r %1 %2) dates)]] ))

(rum/defc day-names-row < {:key-fn (fn [_] "week-days")}
  []
  [:div {:class "calendar-week calendar-day-names"}
   (map #(identity [:div {:class "calendar-day"} %]) day-names)])

(rum/defc month < rum/reactive
  [r]
  (let [date  (rum/react (citrus/subscription r [:calendar-popper :date]))
        month (tu/month->calendar date)]
    [:div [(day-names-row)
           (weeks r 0 (first month) true)
           (map-indexed #(weeks r %1 %2 false) (next month))]]))

(rum/defc Body < rum/reactive
  {:key-fn (fn [_] "body")}
  [r]
  (tc {:component :box
       :opts      {:className "calendar-body"}
       :child     (month r)}))


