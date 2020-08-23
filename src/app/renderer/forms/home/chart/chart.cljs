(ns app.renderer.forms.home.chart.chart
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc]]
            [cljs-time.format :as ft]
            ["@material-ui/core/styles" :refer [styled]]
            ["@material-ui/core" :refer [Box]]
            [cljs-time.core :as tcore]
            [app.renderer.forms.home.chart.timeline :as t]
            [citrus.core :as citrus]))

(defn format-time [time]
  (ft/unparse (ft/formatter "HH:mm") time))

(defn tooltip-body
  [start end code]
  (tc {:component :box
       :opts      {:display        "flex"
                   :alignItems     "center"
                   :flexDirection  "column"
                   :justifyContent "center"}
       :child     [{:component :typography
                    :child     [{:component :typography
                                 :key       "code"
                                 :child     code}
                                {:component :typography
                                 :key       "time"
                                 :child     (str (format-time start) " - " (format-time end))}]}]}))
(rum/defc section < rum/reactive
  [r index row]
  (let [selected  (rum/react (citrus/subscription r [:chart :selected]))
        code      (:code row)
        start     (:start row)
        end       (:end row)
        interval  (:interval row)
        highlight (contains?  selected (:code row))
        theme     (rum/react (citrus/subscription r [:theme :cljs]))
        class     (cond
                    (= "Away" code) "away-block"
                    code            "task-block"
                    :else           (-> theme :palette :backgraound :paper))]
    (tc {:component :tooltip
         :opts      {:title                (tooltip-body start end code)
                     :disableHoverListener (not code)
                     :enterTouchDelay      100
                     :placement            "top"}
         :child     {:component :box
                     :opts      {:flex           (str "0 0 " (str interval "px"))
                                 :justifyContent "center"
                                 :display        "flex"
                                 :alignItems     "center"
                                 :key            (str "task-" index)
                                 :height         "100%"
                                 :className      (str class "  " (when highlight "brightness"))}
                     ;; :child     {:component :box
                     ;;             :styl      {:textOverflow "ellipsis"
                     ;;                         :overflow     "hidden"}
                     ;;             :child     code}
                     }})))

(rum/defc Chart < rum/reactive
  [r height]
  (let [data (rum/react (citrus/subscription r [:chart :chart]))]
    (tc {:component :box
         :opts      {:display "flex"
                     :height  (str height "px")
                     :width   "100%"}
         :child     (map-indexed #(section r %1 %2) data)})))


