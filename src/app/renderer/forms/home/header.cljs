(ns app.renderer.forms.home.header
  (:require [rum.core :as rum]
            ["@date-io/moment" :as MomentUtils]
            ["@material-ui/icons" :refer [ArrowBack ArrowForward Refresh Today SearchOutlined]]
            ["@material-ui/core" :refer [Button Paper IconButton Box]]
            [app.renderer.forms.search :refer [Search]]
            ["@material-ui/pickers" :refer [MuiPickersUtilsProvider KeyboardDatePicker DatePicker]]
            [citrus.core :as citrus]))

(defn to-box [child]
  (rum/adapt-class Box {:p 1} child))

(defn center-flex-box [child]
  (rum/adapt-class Box {:display    "flex"
                        :alignItems "center"}
                   child))

(defn top-panel [{:keys [left right]}]
  (rum/adapt-class Paper {:elevation 3}
                   (rum/adapt-class Box {:display         "flex"
                                         :justify-content "space-between"}
                                    [(center-flex-box (map to-box left))
                                     (center-flex-box (map to-box right))])))

(def picker
  (rum/adapt-class DatePicker
                   {:variant   "inline"
                    :className "datapicker"
                    :margin    "normal"
                    :format    "dddd, MMMM DD, yyyy"
                    :onChange  #(print %)}))

(def provider
  (rum/adapt-class MuiPickersUtilsProvider
                   {:utils #(new MomentUtils %)}
                   picker))

(rum/defc previously-day [r]
  (rum/adapt-class     IconButton
                       {:key     "icon-button"
                        :variant "inline"
                        :title   "Previously day"}
                       (rum/adapt-class   ArrowBack
                                          {})))

(rum/defc next-day [r]
  (rum/adapt-class     IconButton
                       {:key     "icon-button"
                        :variant "inline"
                        :title   "Next day"}
                       (rum/adapt-class   ArrowForward
                                          {})))

(rum/defc today-day [r]
  (rum/adapt-class     IconButton
                       {:key     "icon-button"
                        :variant "inline"
                        :title   "Today"}
                       (rum/adapt-class   Today
                                          {})))
(rum/defc refresh [r]
  (rum/adapt-class     IconButton
                       {:key     "icon-button"
                        :variant "inline"
                        :title   "Refresh"}
                       (rum/adapt-class   Refresh
                                          {})))

(rum/defc search [r]
  (rum/adapt-class     IconButton
                       {:key     "icon-button"
                        :variant "inline"
                        :title   "Refresh"
                        :onClick #(citrus/dispatch! r :home :open-dialog)}
                       (rum/adapt-class   SearchOutlined
                                          {})))

(rum/defc submit-button < rum/reactive
  [r]
  (rum/adapt-class Button
                   {:variant "contained"
                    :color   "primary"
                    :size    "large"
                    :margin  "normal"}
                   "Submit"))

(rum/defc Header < rum/reactive
  [r]
  (to-box (top-panel {:left  [(previously-day r)
                              provider
                              (next-day r)
                              (today-day r)
                              (refresh r)
                              (search r)
                              (Search r)]
                      :right [(submit-button r)]})))

