(ns app.renderer.forms.chart.middle.chart-popper
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc]]
            [app.renderer.time-utils :as tu]
            [citrus.core :as citrus]
            [cljs-time.core :as t]
            [cljs-time.format :as ft]))

(rum/defc body  < rum/reactive
  {:key-fn (fn [_] "body")}
  [r start end task date]
  (let [start-str (tu/to-time->field start)
        end-str   (tu/to-time->field end)
        interval  (tu/format-time (tu/get-interval start end))]
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
                                     :child     "Edit Time"}}
                        {:component :icon-button
                         :opts      {:className "popper-close"
                                     :key       "close"
                                     :onClick   #(citrus/dispatch! r :chart-popper :close-popper)}
                         :child     {:component :close}}]}
           {:component :box
            :opts      {:display        "flex"
                        :key            "time"
                        :alignItems     "center"
                        :pb             2
                        :justifyContent "space-between"}
            :child     [{:component :time-field
                         :opts      {:value     start-str
                                     :key       "start"
                                     :className "time-field"
                                     :onChange  #(citrus/dispatch! r :chart-popper :set-start (tu/merge-date-time date %2))
                                     :colon     ":"}}
                        {:component :time-field
                         :opts      {:value     end-str
                                     :key       "end"
                                     :onChange  #(citrus/dispatch! r :chart-popper :set-end (tu/merge-date-time date %2))
                                     :className "time-field"
                                     :colon     ":"}}]}
           {:component :box
            :opts      {:display        "flex"
                        :key            "footer"
                        :alignItems     "center"
                        :justifyContent "flex-end"}
            :child     [{:component :box
                         :opts      {:key "interval"}
                         :child     {:component :typography
                                     :opts      {:className "popper-interval"}
                                     :child     interval}}
                        {:component :button
                         :opts      {:variant   "contained"
                                     :key       "save"
                                     ;; :onClick   #(save-time task)
                                     :className "popper-save"
                                     :color     "primary"}
                         :child     "Save"}]}]}})))

(rum/defc Popper < rum/reactive
  {:key-fn (fn [_] "popover")}
  [r]
  (let [chart-popper (rum/react (citrus/subscription r [:chart-popper]))
        position     (:position chart-popper)
        open?        (:open chart-popper)
        start        (:start chart-popper)
        end          (:end chart-popper)
        date         (rum/react (citrus/subscription r [:chart :date]))
        task         (:code chart-popper)]
    (when open? (tc {:component :popover
                     :opts      {:open            open?
                                 :anchorReference "anchorPosition"
                                 :anchorPosition  {:top  (:mouseY position)
                                                   :left (:mouseX position)}
                                 :onClose         #(citrus/dispatch! r :chart-popper :close-popper)}
                     :child     (body r start end task date)}))))
