(ns app.renderer.forms.home.statistic.tabs
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc format-time]]
            [cljs-time.format :as ft]
            [cljs-time.core :as tcore]
            ["@material-ui/core/styles" :refer [styled]]
            ["@material-ui/core" :refer [Typography]]
            [citrus.core :as citrus]))

(rum/defc block < rum/reactive
  {:key-fn (fn [_ title _] title)}
  [r title time]
  (let [style (cond
                (= "Tracked" title)   "#3f51b5"
                (= "Submitted" title) "#4caf50"
                (= "Logged" title)    "#9e9e9e")]
    (tc {:component :box
         :opts      {:width "100%"
                     :p     1}
         :child     {:component :box
                     :opts      {:height "100px"
                                 :p      1}
                     :child     {:component :box
                                 :opts      {:display        "flex"
                                             :alignItems     "center"
                                             :flexDirection  "column"
                                             :justifyContent "center"}
                                 :child     [{:component :typography
                                              :key       "title"
                                              :child     title}
                                             {:component :typography
                                              :key       "time"
                                              :child     (format-time time)}]}}})))

(rum/defc logged < rum/reactive
  {:key-fn (fn [_] "logged")}
  [r]
  (let [chart    (rum/react (citrus/subscription r [:chart :chart]))
        selected (rum/react (citrus/subscription r [:chart :selected]))
        time     (->> chart
                      (filter #(not (nil? (:code %))))
                      (filter #(contains? selected (:code %)))
                      (map :interval)
                      (reduce + 0))]
    (block r "Logged" time)))

(rum/defc tracked < rum/reactive
  {:key-fn (fn [_] "tracked")}
  [r]
  (let [chart    (rum/react (citrus/subscription r [:chart :chart]))
        selected (rum/react (citrus/subscription r [:chart :selected]))
        time     (->> chart
                      (filter #(not (nil? (:code %))))
                      (filter #(contains? selected (:code %)))
                      (filter #(not (= "Away" (:code %))))
                      (map :interval)
                      (reduce + 0))]
    (block r "Tracked" time)))

(rum/defc Tabs < rum/reactive
  [r]
  (let [statistic (rum/react (citrus/subscription r [:home :statistic]))]
    (tc {:component :box
         :opts      {:display "flex"}
         :width     "fit-content"
         :child     [(when (contains? statistic :logged) (logged r))
                     (when (contains? statistic :tracked) (tracked r))
                     (when (contains? statistic :submitted) (block  r "Submitted"))]})))
