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
         :opts      {:className "search-close"
                     :onClick   #(do (citrus/dispatch! r :project :init)
                                     (citrus/dispatch! r :router :push :home))}
         :child     {:component :close}})))

(rum/defc title < rum/reactive
  {:key-fn (fn [_] "title")}
  [r]
  (tc {:component :box
       :opts      {:px 2}
       :child     {:component :typography
                   :styl      {:fontWeight "bold"}
                   :opts      {:variant "h6"}
                   :child     "Select Task"}}))

(defn title-box [r]
  (tc {:component :box
       :opts      {:p              1
                   :display        "flex"
                   :key            "title-bar"
                   :alignItems     "center"
                   :justifyContent "space-between"}
       :child     [(title r)
                   (close-button r)]}))

(def load-mixin
  {:will-mount (fn [{[r] :rum/args :as state}]
                 (citrus/dispatch! r :project :get-projects)
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
                    :opts      {:key   "project"
                                :pr    1
                                :width "40%"}
                    :child     (project-search/Search-box r)}
                   {:component :box
                    :opts      {:key   "issue"
                                :width "60%"}
                    :child     (issue-search/Search-box r)}]}))

(rum/defc Search
  [r]
  [(title-box r)
   (loaded-content r)])
