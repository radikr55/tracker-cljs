(ns app.renderer.forms.chart.right.stat-popper
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc]]
            [app.renderer.time-utils :as tu]
            [citrus.core :as citrus]
            [cljs-time.core :as t]
            [cljs-time.format :as ft]))

(rum/defc body < rum/reactive
                 {:key-fn (fn [_] "body")}
  [r time task date]
  (let [time-str (tu/format-time->field time)]
    (tc {:component :paper
         :opts      {:className "chart-popper-paper"}
         :child
                    {:component :box
                     :opts      {:width     200
                                 :className "chart-popper-box"}
                     :child
                                [{:component :box
                                  :opts      {:display        "flex"
                                              :key            "title"
                                              :alignItems     "center"
                                              :pb             2
                                              :justifyContent "space-between"}
                                  :child     [{:component :box
                                               :opts      {:key "title"}
                                               :child     {:component :typography
                                                           :opts      {:className "popper-title"}
                                                           :child     "Submit to Jira"}}
                                              {:component :icon-button
                                               :opts      {:className "popper-close"
                                                           :key       "close"
                                                           :onClick   #(citrus/dispatch! r :stat-popper :close-popper)}
                                               :child     {:component :close}}]}
                                 {:component :box
                                  :opts      {:display        "flex"
                                              :key            "footer"
                                              :alignItems     "center"
                                              :justifyContent "space-between"}
                                  :child     [{:component :time-field
                                               :opts      {:value       time-str
                                                           :key         "end"
                                                           :onMouseOver #(.focus (.-target %))
                                                           :onWheel     #(tu/wheel->interval-field r % time :set-time :stat-popper)
                                                           :onChange    #(citrus/dispatch! r :stat-popper :set-time (tu/to-interval->field %2))
                                                           :className   "time-field"
                                                           :colon       ":"}}
                                              {:component :button
                                               :opts      {:variant   "contained"
                                                           :key       "save"
                                                           :onClick   #(citrus/dispatch! r :stat-popper :send-time date)
                                                           :className "popper-save"
                                                           :color     "primary"}
                                               :child     "Submit"}]}]}})))

(rum/defc Popper < rum/reactive
                   {:key-fn (fn [_] "popover")}
  [r]
  (let [stat-popper (rum/react (citrus/subscription r [:stat-popper]))
        list        (rum/react (citrus/subscription r [:chart :list]))
        position    (:position stat-popper)
        open?       (:open stat-popper)
        time        (:time stat-popper)
        date        (rum/react (citrus/subscription r [:chart :date]))
        task        (:code stat-popper)]
    (when open? (tc {:component :popover
                     :opts      {:open            open?
                                 :anchorReference "anchorPosition"
                                 :anchorPosition  {:top  (:mouseY position)
                                                   :left (:mouseX position)}
                                 :onClose         #(citrus/dispatch! r :stat-popper :close-popper)}
                     :child     (body r time task date)}))))
