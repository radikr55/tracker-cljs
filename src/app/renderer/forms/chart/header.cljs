(ns app.renderer.forms.chart.header
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc]]
            ["@date-io/moment" :as MomentUtils]
            ["@material-ui/pickers" :refer [MuiPickersUtilsProvider KeyboardDatePicker DatePicker]]
            [citrus.core :as citrus]))

(rum/defc picker < rum/reactive
  [r]
  (tc  {:component :date-picker
        :opts      {:margin       "normal"
                    :className    "date-picker"
                    :variant      "inline"
                    :inputVariant "outlined"
                    :format       "dddd, MMMM DD, yyyy"
                    :onChange     #(print %)}}))

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
  (tc {:component :box
       :opts      {:display    "flex"
                   :width      "100%"
                   :alignItems "center"}
       :child     [(provider r)
                   {:component :button
                    :opts      {:key       "left-button"
                                :className "header-button"}
                    :child     {:component :arrow-left}}
                   {:component :button
                    :opts      {:key       "right-button"
                                :onClick   #(citrus/dispatch! r :theme :switch)
                                :className "header-button"}
                    :child     {:component :arrow-right}}
                   {:component :button
                    :styl      {:font-weight "900"}
                    :opts      {:variant   "contained"
                                :className "header-button"
                                :key       "today-button"}
                    :child     "today"}]}))

(rum/defc right < rum/reactive
  {:key-fn (fn [_] "right")}
  [r]
  (tc {:component :box
       :opts      {:display        "flex"
                   :width          "100%"
                   :justifyContent "flex-end"
                   :alignItems     "center"}
       :child     [{:component :button
                    :styl      {:font-weight "900"}
                    :opts      {:variant   "contained"
                                :className "header-button"
                                :key       "submit"
                                :color     "primary"}
                    :child     "submit"}]}))

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
