(ns app.renderer.forms.search
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            ["@material-ui/core" :refer [Typography Box DialogTitle Dialog AppBar Toolbar Slide IconButton]]
            ["@material-ui/icons" :refer [Close]]
            ["@material-ui/core/styles" :refer [styled]]
            [app.renderer.forms.search.project-search :as project-search]
            [app.renderer.forms.search.issue-search :as issue-search]))

(rum/defc dialog-close-button < rum/reactive
  [r]
  (let [theme (rum/react (citrus/subscription r [:home :theme]))]
    (rum/adapt-class ((styled  IconButton)
                      #(clj->js {:position "absolute"
                                 :right    ((-> theme :spacing) 1)
                                 :top      ((-> theme :spacing) 1)}))
                     {:onClick #(citrus/dispatch! r :home :close-dialog)}
                     (rum/adapt-class Close {}))))

(rum/defc dialog-title < rum/reactive
  [r]
  (rum/adapt-class DialogTitle
                   {:onClose #(citrus/dispatch! r :home :close-dialog)
                    :id      "customized-dialog-title"}
                   
                   [(rum/adapt-class Typography {:variant "h6"}
                                     "Search")
                    (dialog-close-button r)]))

(rum/defc Search < rum/reactive
  [r]
  (let [{open-dialog :search-dialog} (rum/react (citrus/subscription r [:home]))]
    (rum/adapt-class Dialog {:className       "search-dialog"
                             :open            open-dialog
                             :fullWidth       true
                             :aria-labelledby "customized-dialog-title"
                             :maxWidth        "xl"
                             :onClose         #(citrus/dispatch! r :home :close-dialog)}
                     [(dialog-title r)
                      (rum/adapt-class Box {:display         "flex"
                                            :justify-content "space-between"}
                                       (project-search/Search-box r)
                                       (issue-search/Search-box r))])))
