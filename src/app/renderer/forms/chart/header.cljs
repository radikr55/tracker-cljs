(ns app.renderer.forms.chart.header
  (:require [rum.core :as rum]
            [cljs-time.core :as t]
            [cljs-time.coerce :as c]
            [app.renderer.utils :refer [tc]]
            [app.renderer.time-utils :as tu]
            ["@date-io/moment" :as MomentUtils]
            [citrus.core :as citrus]))

(rum/defc picker < rum/reactive
  [r]
  (let [date (rum/react (citrus/subscription r [:chart :date]))]
    (tc  {:component :date-picker
          :opts      {:margin         "normal"
                      :className      "date-picker"
                      :variant        "inline"
                      :inputVariant   "outlined"
                      :value          (c/to-date date)
                      :autoOk         true
                      :disableFuture  true
                      :disableToolbar true
                      :format         "dddd, MMMM DD, yyyy"
                      :onChange       #(do (citrus/dispatch! r :chart :set-date (c/from-date (.toDate %)))
                                           (citrus/dispatch! r :chart :load-track-logs))}})))

(rum/defc provider < rum/reactive
  {:key-fn (fn [_] "provider")}
  [r]
  (tc {:component :date-provider
       :opts      {:utils #(new MomentUtils %)
                   :key   "provider"}
       :child     (picker r)}))

(rum/defc left < rum/reactive
  {:key-fn (fn [_] "left")}
  [r]
  (let [date (rum/react (citrus/subscription r [:chart :date]))]
    (tc {:component :box
         :opts      {:display    "flex"
                     :width      "100%"
                     :alignItems "center"}
         :child     [(provider r)
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
                      :child     "today"}]}) ))

(rum/defc sum-time < rum/static
  {:key-fn (fn [type] type)}
  [type time]
  (tc {:component :box
       :opts      {:className (str  "sum-statistic sum-statistic-" type)}
       :child     [{:component :typography
                    :opts      {:key       (str type "time")
                                :className "sum-time"}
                    :child     (tu/format-time time)}
                   {:component :typography
                    :opts      {:key       (str type "title")
                                :className "sum-title"}
                    :child     type}]}))

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
         :child     [(sum-time "logged" logged)
                     (sum-time "tracked" tracked)
                     (sum-time "submitted" submitted)
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
