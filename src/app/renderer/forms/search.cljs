(ns app.renderer.forms.search
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [app.renderer.utils :refer [tc]]
            ["@material-ui/core/styles" :refer [styled]]
            [app.renderer.forms.search.project-search :as project-search]
            [app.renderer.forms.search.issue-search :as issue-search]))

(rum/defc close-button < rum/reactive
  {:key-fn (fn [_] "close")}
  [r]
  (let [theme (rum/react (citrus/subscription r [:home :theme]))]
    (tc {:component :icon-button
         :opts      {:onClick #(citrus/dispatch! r :router :push :home)}
         :child     {:component :close}})))

(rum/defc title < rum/reactive
  {:key-fn (fn [_] "title")}
  [r]
  (tc {:component :box
       :opts      {:px 2}
       :child     {:component :typography
                   :opts      {:variant "h6"}
                   :child     "Search"}}))

(defn title-box [r]
  (tc {:component :box
       :opts      {:p   1
                   :key "title-bar"}
       :child     {:component :paper
                   :opts      {:elevation 4}
                   :child     {:component :box
                               :opts      {:p              1
                                           :display        "flex"
                                           :alignItems     "center"
                                           :justifyContent "space-between"}
                               :child     [(title r)
                                           (close-button r)]}}}))

(rum/defc content
  [component]
  (tc {:component :paper
       :child     {:component :box
                   :opts      {:p 1}
                   :child     component}}))

(def load-mixin
  {:will-mount (fn [{[r] :rum/args :as state}]
                 (citrus/dispatch! r :project :get)
                 state)})

(rum/defc loaded-content < load-mixin
  {:key-fn (fn [_] "content")}
  [r]
  (tc {:component :box
       :opts      {:p              1
                   :display        "flex"
                   :alignItems     "center"
                   :justifyContent "space-between"}
       :child     [{:component :box
                    :opts      {:pr    1
                                :key   "project"
                                :width "60%"}
                    :child     (content (project-search/Search-box r) )}
                   {:component :box
                    :opts      {:pl    1
                                :key   "issue"
                                :width "100%"}
                    :child     (content (issue-search/Search-box r) )}]}))

(rum/defc Search
  [r]
  [(title-box r)
   (loaded-content r)])
