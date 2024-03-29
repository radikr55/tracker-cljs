(ns app.renderer.utils
  (:require
   ["react-draggable" :default Draggable]
   ["react-simple-timefield" :default TimeField]
   ["@material-ui/core/styles" :refer [styled]]
   ["@material-ui/lab" :refer [Alert]]
   ["react-custom-scrollbars" :default Scrollbars]
   ["@material-ui/core" :refer [Typography Box
                                Fade LinearProgress
                                InputAdornment TextField
                                DialogTitle Dialog
                                DialogContent DialogActions
                                Popper Menu MenuItem
                                ListItemSecondaryAction
                                Snackbar ListItemIcon
                                Popover Divider Switch List ListItem
                                Button ButtonBase Tooltip
                                ListItemText ListSubheader
                                FormControl Badge FormGroup FormControlLabel
                                Paper Fab Grid FormGroup
                                CircularProgress IconButton]]
   ["@material-ui/icons" :refer [Close Add Search
                                 MoreVert OpenInNew
                                 FiberManualRecord
                                 SettingsEthernetSharp
                                 Visibility Sort
                                 Cancel CheckCircle
                                 ArrowForward ArrowBack
                                 ArrowForwardIos ArrowBackIos
                                 ArrowDownward ArrowUpward
                                 DeleteForeverOutlined
                                 VisibilityOutlined
                                 ZoomIn ZoomOut]]))

(defn get-component [key]
  (case key
    :dialog-title               DialogTitle
    :dialog                     Dialog
    :dialog-content             DialogContent
    :dialog-actions             DialogActions
    :fade                       Fade
    :linear-progress            LinearProgress
    :draggable                  Draggable
    :time-field                 TimeField
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
    :fab                        Fab
    :add                        Add
    :tooltip                    Tooltip
    :popper                     Popper
    :sort                       Sort
    :arrow-up                   ArrowUpward
    :arrow-back                 ArrowBack
    :arrow-forward              ArrowForward
    :arrow-left                 ArrowBackIos
    :arrow-right                ArrowForwardIos
    :grid                       Grid
    :arrow-down                 ArrowDownward
    :more-vert                  MoreVert
    :dot                        FiberManualRecord
    :list-item-icon             ListItemIcon
    :menu                       Menu
    :menu-item                  MenuItem
    :open-in-new                OpenInNew
    :search                     Search
    :input-adornment            InputAdornment
    :zoom-in                    ZoomIn
    :zoom-out                   ZoomOut
    :snack                      Snackbar
    :alert                      Alert
    :badge                      Badge
    :circular-progress          CircularProgress
    :scrollbars                 Scrollbars
    :div                        "div"
    nil))

(defn obj-js [component]
  (let [opts  (:opts component)
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
      (js/React.createElement ((styled comp)
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

(defn scroll-vertical-box [right boxes]
  (doseq [box boxes
          :let [current (.-current box)
                r-current (.-current right)]]
    (when (and (not (nil? r-current)) (not (nil? current)))
      (set! (.-scrollTop current) (.-scrollTop (.-view (.-current right)))))))

(defn package-config [config]
  (get (js->clj (js/require "../../package.json")) config))
