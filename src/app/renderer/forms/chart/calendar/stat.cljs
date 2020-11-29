(ns app.renderer.forms.chart.calendar.stat
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [app.renderer.utils :refer [tc]]
            [app.renderer.time-utils :as tu]))

(rum/defc sum-week < rum/reactive
  {:key-fn (fn [_] "month")}
  [r]
  (let [stat      (rum/react (citrus/subscription r [:calendar-popper :stat]))
        count-fn  (fn [key] (->> stat
                                 (into [])
                                 (map second)
                                 (map key)
                                 (reduce + 0)))
        submitted (count-fn :submitted)
        logged    (count-fn :logged)
        tracked   (count-fn :tracked)]
    [:div {:class "calendar-stat-week calendar-week calendar-stat-sum"}
     [:div {:class "sum-statistic-logged calendar-stat-cell"} (tu/format-time (/ logged 60))]
     [:div {:class "sum-statistic-tracked calendar-stat-cell"} (tu/format-time (/ tracked 60))]
     [:div {:class "sum-statistic-submitted calendar-stat-cell"} (tu/format-time (/ submitted 60))]]))

(rum/defc week < rum/reactive
  {:key-fn (fn [_ index] (str "week" index))}
  [r index [week-num _]]
  (let [stat        (rum/react (citrus/subscription r [:calendar-popper :stat]))
        week        ((keyword (str week-num)) stat)
        submitted   (:submitted week)
        logged      (:logged week)
        tracked     (:tracked week)
        select-week (rum/react (citrus/subscription r [:calendar-popper :select-week]))
        class       (cond-> "calendar-stat-week calendar-week "
                      (= week-num select-week) (str " calendar-select-week"))]
    (if (->> [logged tracked submitted]
             (reduce +)
             (not= 0))
      [:div {:class class}
       [:div {:class "sum-statistic-logged calendar-stat-cell"} (tu/format-time (/ logged 60))]
       [:div {:class "sum-statistic-tracked calendar-stat-cell"} (tu/format-time (/ tracked 60))]
       [:div {:class "sum-statistic-submitted calendar-stat-cell"} (tu/format-time (/ submitted 60))]]
      [:div {:class class}])))

(rum/defc month < rum/reactive
  [r]
  (let [date  (rum/react (citrus/subscription r [:calendar-popper :date]))
        month (tu/month->calendar date)]
    [:div [(sum-week r)
           (map-indexed #(week r %1 %2) month)]]))

(rum/defc Stat < rum/reactive
  {:key-fn (fn [_] "stat")}
  [r]
  (tc {:component :box
       :opts      {:className "calendar-stat"}
       :child     (month r)}))
