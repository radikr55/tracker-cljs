(ns app.renderer.forms.search
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            ["@material-ui/core" :refer [Typography Box
                                         DialogTitle Dialog
                                         AppBar Toolbar
                                         Paper Collapse
                                         Slide IconButton]]
            ["@material-ui/icons" :refer [Close]]
            ["@material-ui/core/styles" :refer [styled]]
            [app.renderer.forms.search.project-search :as project-search]
            [app.renderer.forms.search.issue-search :as issue-search]))

(defn get-component [key]
  (case key
    :box   Box
    :paper Paper))

(defn tc [[component-key opts child]]
  (let [opt  (if opts opts {})
        comp (get-component component-key)]
    (js/React.createElement comp (clj->js opts) child)))

(defn to-box-content
  ([child]
   (tc [:box {:p 1 :width "100%"} child]))
  ([child width] (tc [:box {:p     1
                            :width width} child])))

(defn paper [child]
  (tc  [:paper {:elevation 3} child]))

(defn to-box [child]
  (tc [:box {:display         "flex"
             :justify-content "space-between"} child]))

(defn to-title-box [child]
  (rum/adapt-class Box {:p 1}
                   (rum/adapt-class Paper {:elevation 4}
                                    (rum/adapt-class Box {:p               1
                                                          :display         "flex"
                                                          :alignItems      "center"
                                                          :justify-content "space-between"}
                                                     child))))

(rum/defc close-button < rum/reactive
  [r]
  (let [theme (rum/react (citrus/subscription r [:home :theme]))]
    (rum/adapt-class  IconButton
                      {:onClick #(citrus/dispatch! r :router :push :home)}
                      (rum/adapt-class Close {}))))

(rum/defc title < rum/reactive
  [r]
  (rum/adapt-class Box {:px 2}
                   (rum/adapt-class Typography {:variant "h6"}
                                    "Search")))
(rum/defc to-content
  [child width]
  (let [search (paper (:search child))
        table  (paper (:table child))]
    (to-box-content (paper [(to-box-content search)
                            (to-box-content table)])  width)))

(def load-mixin
  {:will-mount (fn [{[r] :rum/args :as state}]
                 (citrus/dispatch! r :project :get)
                 state)})

(rum/defc loaded-content < load-mixin
  [r]
  (to-box
    [(to-content (project-search/Search-box r) "70%")
     (to-content (issue-search/Search-box r) "100%")]))

(rum/defc Search
  [r]
  [(to-title-box
     [(rum/with-key (title r) "title")
      (rum/with-key (close-button r) "close-button")])
   (loaded-content r)])
