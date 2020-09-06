(ns app.renderer.utils
  (:require
   [goog.string :as gstring]
   [goog.string.format]
   ["react-draggable" :default Draggable]
   ["@material-ui/core/styles" :refer [styled]]
   ["@material-ui/pickers" :refer [MuiPickersUtilsProvider KeyboardDatePicker DatePicker]]
   ["@material-ui/core" :refer [Typography Box
                                DialogTitle Dialog
                                InputAdornment TextField
                                Popper Menu MenuItem
                                ListItemSecondaryAction
                                ListItemIcon
                                Popover Divider
                                Switch List ListItem
                                Button ButtonBase Tooltip
                                ListItemText ListSubheader
                                AppBar Toolbar FormControl
                                FormGroup FormControlLabel
                                Paper Collapse Fab
                                Grid FormGroup
                                Slide IconButton]]
   ["@material-ui/icons" :refer [Close Add Search
                                 MoreVert OpenInNew
                                 FiberManualRecord
                                 SettingsEthernetSharp
                                 Visibility Sort
                                 Cancel CheckCircle
                                 ArrowForwardIos ArrowBackIos
                                 ArrowDownward ArrowUpward
                                 DeleteForeverOutlined
                                 VisibilityOutlined
                                 ZoomIn ZoomOut]]))

(defn get-component [key]
  (case key
    :draggable                  Draggable
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
    :arrow-left                 ArrowBackIos
    :arrow-right                ArrowForwardIos
    :grid                       Grid
    :arrow-down                 ArrowDownward
    :date-provider              MuiPickersUtilsProvider
    :date-picker                DatePicker
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
    (if time (gstring/format "%02dh %02dm" hour minutes)
        "00h 00m")))

(defn scroll-vertical-box
  [posr ref]
  (let [current (.-current ref)]
    (when (not (nil? current))
      (let [scroll-position (.-scrollTop current)
            pos             (+ posr scroll-position)]
        (set! (.-scrollTop current) pos)))))

(defn on-wheel-vertical [e boxes]
  (let [delta (.-deltaY e)]
    (doseq [box boxes]
      (if (> delta 0)
        (scroll-vertical-box 30 box)
        (scroll-vertical-box -30 box)))))
