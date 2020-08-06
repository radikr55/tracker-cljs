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

(defn to-box [child]
  (rum/adapt-class Box {:display         "flex"
                        :justify-content "space-between"} child))

(defn to-title-box [child]
  (rum/adapt-class Box {:px 1
                        :py 1}
                   (rum/adapt-class Paper {:elevation 5}
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
                                    "Search") ))

(rum/defc Search 
  [r]
  [(to-title-box
     [(rum/with-key (title r) "title")
      (rum/with-key (close-button r) "close-button")])
   (to-box
     [(rum/with-key (project-search/Search-box r) "project-search")
      (rum/with-key (issue-search/Search-box r) "issue-search")])])
