(ns app.renderer.forms.components
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            ["@material-ui/pickers" :refer [MuiPickersUtilsProvider KeyboardDatePicker]]
            ["@date-io/moment" :as MomentUtils]
            ["@material-ui/core" :refer [Container
                                         Avatar
                                         Button
                                         TextField
                                         Typography
                                         createMuiTheme]]))

(defn rc [{:keys [el props child]}] (js/React.createElement el (clj->js props) child))

(rum/defc form [props child]
  [:form props  child])

(rum/defc div [props child]
  [:div props child])

(defn typography [props child]
  (rc {:el    Typography
       :props props
       :child child}))

(defn text-field [props]
  (rc {:el    TextField
       :props props}))

(defn button [props child]
  (rc {:el    Button
       :props props
       :child child}))

(defn container [props child]
  (rc {:el    Container
       :props props
       :child child}))
