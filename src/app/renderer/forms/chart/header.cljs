(ns app.renderer.forms.chart.header
  (:require [rum.core :as rum]
            [cljs-time.core :as t]
            [app.renderer.utils :refer [tc]]
            [app.renderer.time-utils :as tu]
            [app.renderer.forms.chart.calendar.calendar :refer [Calendar]]
            [citrus.core :as citrus]))

(defn open-calendar [r event date]
  (citrus/dispatch! r :calendar-popper :open-popper
                    {:position {:mouseX (.-clientX event)
                                :mouseY (.-clientY event)}
                     :date     date})
  (citrus/dispatch! r :calendar-popper :load-stat))

(rum/defc left < rum/reactive
  {:key-fn (fn [_] "left")}
  [r]
  (let [date               (rum/react (citrus/subscription r [:chart :date]))
        not-submitted      (rum/react (citrus/subscription r [:chart :not-submitted]))
        not-submitted-list (->> not-submitted
                                (map #(tu/date->calendar-date %))
                                (reduce #(str %1 "\n" %2)))]
    (tc {:component :box
         :opts      {:display    "flex"
                     :width      "100%"
                     :alignItems "center"}
         :child     [(Calendar r)
                     {:component :badge
                      :opts      {:className    "badge"
                                  :key          "calendar-field"
                                  :color        "error"
                                  :badgeContent (count not-submitted)
                                  :title        not-submitted-list
                                  :invisible    (empty? not-submitted)}
                      :child     {:component :button
                                  :opts      {:key       "calendar-button"
                                              :variant   "contained"
                                              :className "header-button calendar-field"
                                              :onClick   #(open-calendar r % date)}
                                  :child     (tu/date->calendar-field date)}}
                     {:component :button
                      :opts      {:key       "left-button"
                                  :onClick   #(do (citrus/dispatch! r :chart :dec-date)
                                                  (citrus/dispatch! r :chart :load-track-logs))
                                  :className "header-button left-button calendar-button MuiButton-contained"}
                      :child     {:component :arrow-left}}
                     {:component :button
                      :opts      {:key       "right-button"
                                  :disabled  (tu/eq-by-date (t/now) date)
                                  :onClick   #(do (citrus/dispatch! r :chart :inc-date)
                                                  (citrus/dispatch! r :chart :load-track-logs))
                                  :className "header-button calendar-button MuiButton-contained"}
                      :child     {:component :arrow-right}}
                     {:component :button
                      :styl      {:font-weight "900"}
                      :opts      {:variant   "contained"
                                  :disabled  (tu/eq-by-date (t/now) date)
                                  :className "header-button"
                                  :onClick   #(do (citrus/dispatch! r :chart :set-date (t/now))
                                                  (citrus/dispatch! r :chart :load-track-logs))
                                  :key       "today-button"}
                      :child     "today"}]})))

(rum/defc sum-time < rum/reactive
  {:key-fn (fn [_ type] type)}
  [r type time]
  (let [date          (rum/react (citrus/subscription r [:chart :date]))
        not-submitted (rum/react (citrus/subscription r [:chart :not-submitted]))
        class         (cond-> (str  "sum-statistic sum-statistic-" type)
                        (and (= "submitted" type)
                             (some #(t/= date %) not-submitted)) (str  " sum-statistic-not-submitted "))]
    (tc {:component :box
         :opts      {:className class}
         :child     [{:component :typography
                      :opts      {:key       (str type "time")
                                  :className "sum-time"}
                      :child     (tu/format-time time)}
                     {:component :typography
                      :opts      {:key       (str type "title")
                                  :className "sum-title"}
                      :child     type}]})))

(rum/defc right < rum/reactive
  {:key-fn (fn [_] "right")}
  [r]
  (let [logged    (rum/react (citrus/subscription r [:chart :logged]))
        tracked   (rum/react (citrus/subscription r [:chart :tracked]))
        submitted (rum/react (citrus/subscription r [:chart :submitted]))]
    (tc {:component :box
         :opts      {:display        "flex"
                     :width          "100%"
                     :justifyContent "flex-end"
                     :alignItems     "center"}
         :child     [(sum-time r "logged" logged)
                     (sum-time r "tracked" tracked)
                     (sum-time r "submitted" submitted)
                     {:component :button
                      :opts      {:variant   "contained"
                                  :className "header-button submit"
                                  :key       "submit"
                                  :onClick   #(citrus/dispatch! r :chart :submit-all r)
                                  :color     "primary"}
                      :child     "submit all"}]})))

(rum/defc Header < rum/reactive
  {:key-fn (fn [_] "header")}
  [r h-header]
  (tc {:component :box
       :opts      {:display         "flex"
                   :height          (str h-header "px")
                   :width           "100%"
                   :padding         "15px 10px"
                   :justify-content "space-between"}
       :child     [(left r)
                   (right r)]}))
