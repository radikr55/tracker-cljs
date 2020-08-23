(ns app.renderer.utils
  (:require
   [goog.string :as gstring]
   [goog.string.format]
   ["@material-ui/core/styles" :refer [styled]]
   ["@material-ui/core" :refer [Typography Box
                                DialogTitle Dialog
                                TextField
                                ListItemSecondaryAction
                                Popover Divider
                                Switch List ListItem
                                Button ButtonBase Tooltip
                                ListItemText ListSubheader
                                AppBar Toolbar FormControl
                                FormGroup FormControlLabel
                                Paper Collapse
                                Grid FormGroup
                                Slide IconButton]]
   ["@material-ui/icons" :refer [Close
                                 SettingsEthernetSharp
                                 Visibility Sort
                                 Cancel CheckCircle
                                 ArrowDownward
                                 AddCircleOutline ArrowUpward
                                 DeleteForeverOutlined
                                 VisibilityOutlined]]))

(defn get-component [key]
  (case key
    :list                       List
    :list-item                  ListItem
    :list-subheader             ListSubheader
    :list-item-text             ListItemText
    :list-item-secondary-action ListItemSecondaryAction
    :delete-forever-outlined    DeleteForeverOutlined
    :visibility                 Visibility
    :setting-sharp              SettingsEthernetSharp
    :visibility-outlined        VisibilityOutlined
    :box                        Box
    :form-group                 FormGroup
    :form-control-label         FormControlLabel
    :form-control               FormControl
    :switch                     Switch
    :popover                    Popover
    :divider                    Divider
    :typography                 Typography
    :text-field                 TextField
    :paper                      Paper
    :icon-button                IconButton
    :button                     Button
    :button-base                ButtonBase
    :close                      Close
    :cancel                     Cancel
    :check                      CheckCircle
    :tooltip                    Tooltip
    :sort                       Sort
    :arrow-up                   ArrowUpward
    :grid                       Grid
    :arrow-down                 ArrowDownward
    :add                        AddCircleOutline
    nil))

(defn obj-js [component]
  (let [opts  (:opts  component)
        comp  (:component component)
        child (:child component)]
    (if (nil? comp)
      component
      (js/React.createElement (get-component comp) (clj->js opts) child))))

(defn tc [component]
  (let [opts  (:opts component)
        comp  (get-component (:component component))
        child (:child component)
        styl  (:styl component)]
    (if styl
      (js/React.createElement ((styled  comp)
                               #(clj->js styl))
                              (clj->js opts)
                              (cond
                                (map? child)    (js/React.createElement comp (clj->js opts) (tc child))
                                (vector? child) (js/React.createElement comp (clj->js opts) (map tc child))
                                :else           child))
      (cond
        (nil? comp)         component
        (object? component) component
        (map? child)        (js/React.createElement comp (clj->js opts) (tc child))
        (vector? child)     (js/React.createElement comp (clj->js opts) (map tc child))
        :else               (js/React.createElement comp (clj->js opts) child)))))

(defn format-time [time]
  (let [hour    (/ time 60)
        minutes (rem time 60)]
    (if time (gstring/format "%dh %dm" hour minutes)
        "0h 0m")))
