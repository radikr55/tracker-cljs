(ns app.renderer.forms.search
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [app.renderer.utils :refer [tc]]
            [app.renderer.forms.search.project-search :as project-search]
            [app.renderer.forms.search.issue-search :as issue-search]))

(rum/defc close-button < rum/reactive
  {:key-fn (fn [_] "close")}
  [r]
  (tc {:component :icon-button
       :opts      {:className "search-close"
                   :onClick   #(do (citrus/dispatch! r :project :init)
                                   (citrus/dispatch! r :router :push :home)
                                   (reset! project-search/selected-project nil)
                                   )}
       :child     {:component :close}}))

(rum/defc title < rum/reactive
  {:key-fn (fn [_] "title")}
  [_]
  (tc {:component :box
       :opts      {:px 1}
       :child     {:component :typography
                   :opts      {:className "search-title"}
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
       :opts      {:px             2
                   :pb             1
                   :display        "flex"
                   :alignItems     "center"
                   :justifyContent "space-between"}
       :child     [{:component :box
                    :opts      {:key   "project"
                                :pr    1
                                :width "30%"}
                    :child     (project-search/Search-box r)}
                   {:component :box
                    :opts      {:key   "issue"
                                :width "70%"}
                    :child     (issue-search/Search-box r)}]}))

(rum/defc Search
  [r]
  [(title-box r)
   (loaded-content r)])
