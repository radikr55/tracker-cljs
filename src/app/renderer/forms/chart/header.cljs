(ns app.renderer.forms.chart.header
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc]]
            ["@date-io/moment" :as MomentUtils]
            ["@material-ui/pickers" :refer [MuiPickersUtilsProvider KeyboardDatePicker DatePicker]]
            [citrus.core :as citrus]))

(rum/defc picker < rum/reactive
  [r]
  (tc  {:component :date-picker
        :styl {:width        "250px"}
        :opts      {:className    "datapicker"
                    :margin       "normal"
                    :variant      "inline"
                    :inputVariant "outlined"
                    :format       "dddd, MMMM DD, yyyy"
                    :onChange     #(print %)}}))

(rum/defc provider < rum/reactive
  {:key-fn (fn [_] "provider")}
  [r]
  (tc {:component :date-provider
       :opts      {:utils  #(new MomentUtils %)
                   :margin "none"}
       :child     (picker r)}))

(rum/defc left < rum/reactive
  {:key-fn (fn [_] "left")}
  [r]
  (tc {:component :box
       :opts      {:display    "flex"
                   :width "100%"
                   :alignItems "center"}
       :child     [{:component :icon-button
                    :child     {:component :arrow-left}}
                   (provider r)
                   {:component :icon-button
                    :child     {:component :arrow-right}}
                   {:component :button
                    :styl      {:font-weight "900"
                                :height  "100%"}
                    :opts      {:variant "contained"}
                    :child     "today"}]}))

(rum/defc right < rum/reactive
  {:key-fn (fn [_] "right")}
  [r]
  (tc {:component :box
       :opts      {:display    "flex"
                   :width "100%"
                   :justifyContent "flex-end"
                   :alignItems "center"}
       :child [{:component :button
                :styl      {:font-weight "900"
                            :height      "100%"}
                :opts      {:variant "contained"
                            :color "primary"}
                :child     "submit"}]}))

(rum/defc Header < rum/reactive
  [r]
  (tc {:component :box
       :opts      {:display         "flex"
                   :width "100%"
                   :p               1
                   :justify-content "space-between"}
       :child     [(left r)
                   (right r)]}))

;; (defn to-box [child]
;;   (rum/adapt-class Box {:p 1} child))

;; (defn center-flex-box [child]
;;   (rum/adapt-class Box {:display    "flex"
;;                         :alignItems "center"}
;;                    child))

;; (defn top-panel [{:keys [left right]}]
;;   (rum/adapt-class  AppBar {:elevation 4}
;;                     (rum/adapt-class Box {:display         "flex"
;;                                           :justify-content "space-between"}
;;                                      [(center-flex-box (map to-box left))
;;                                       (center-flex-box (map to-box right))])))

;; (rum/defc picker < rum/reactive
;;   [r]
;;   (rum/adapt-class DatePicker
;;                    {
;;                     :className "datapicker"
;;                     :margin    "normal"
;;                     :format    "dddd, MMMM DD, yyyy"
;;                     :onChange  #(print %)}))

;; (rum/defc provider < rum/reactive
;;   [r]
;;   (rum/adapt-class MuiPickersUtilsProvider
;;                    {:utils   #(new MomentUtils %)
;;                     :variant "outlined"
;;                     }
;;                    (picker r)))

;; (rum/defc previously-day [r]
;;   (rum/adapt-class     IconButton
;;                        {:key     "icon-button"
;;                         :variant "inline"
;;                         :title   "Previously day"}
;;                        (rum/adapt-class   ArrowBack
;;                                           {})))

;; (rum/defc next-day [r]
;;   (rum/adapt-class     IconButton
;;                        {:key     "icon-button"
;;                         :variant "inline"
;;                         :title   "Next day"}
;;                        (rum/adapt-class   ArrowForward
;;                                           {})))

;; (rum/defc today-day [r]
;;   (rum/adapt-class     IconButton
;;                        {:key     "icon-button"
;;                         :variant "inline"
;;                         :title   "Today"}
;;                        (rum/adapt-class   Today
;;                                           {})))
;; (rum/defc refresh [r]
;;   (rum/adapt-class     IconButton
;;                        {:key     "icon-button"
;;                         :variant "inline"
;;                         :title   "Refresh"}
;;                        (rum/adapt-class   Refresh
;;                                           {})))

;; (rum/defc search [r]
;;   (rum/adapt-class     IconButton
;;                        {:key     "icon-button"
;;                         :variant "inline"
;;                         :title   "Refresh"
;;                         :onClick #(citrus/dispatch! r :router :push :search)}
;;                        (rum/adapt-class   SearchOutlined
;;                                           {})))

;; (rum/defc submit-button < rum/reactive
;;   [r]
;;   (rum/adapt-class Button
;;                    {:variant "contained"
;;                     :color   "primary"
;;                     :size    "large"
;;                     :margin  "normal"}
;;                    "Submit"))

;; (rum/defc Header < rum/reactive
;;   [r]
;;   (to-box (top-panel {:left  [(rum/with-key (previously-day r) "previously-day")
;;                               (rum/with-key (provider r) "provider")
;;                               (rum/with-key (next-day r) "next-day")
;;                               (rum/with-key (today-day r) "today-day")
;;                               (rum/with-key (refresh r) "refresh")
;;                               (rum/with-key (search r) "search")]
;;                       :right [(submit-button r)]})))

