(ns app.renderer.forms.search.issue-search
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            ["@material-ui/core/styles" :refer [styled]]
            ["@material-ui/core" :refer [Paper
                                         ListSubheader
                                         Box
                                         List
                                         ListItem
                                         ListItemText
                                         TextField]]))

(def search-atom (atom ""))

(rum/defc list-item < rum/reactive
  [r title]
  (rum/adapt-class ListItem {:button true}
                   (rum/adapt-class ListItemText
                                    {:primary (str "test" title)})))
(rum/defc item < rum/reactive
  [r data]
  (let [title (:title data)
        id    (:id data)]
    (rum/adapt-class ListItem {:button    true
                               :className "table-row"}
                     (rum/adapt-class ListItemText
                                      {:secondary title}))))

(rum/defc subheader < rum/reactive
  [r category paper]
  (rum/adapt-class ((styled  ListSubheader)
                    #(clj->js {:backgroundColor paper})) {}
                   category))

(rum/defc table < rum/reactive
  [r]
  (let [{list :right} (rum/react (citrus/subscription r [:project]))
        theme         (rum/react (citrus/subscription r [:theme :cljs]))
        paper         (-> theme :palette :background :paper)
        search        (rum/react search-atom)]
    (rum/adapt-class List {:component "nav"
                           :className "search-list"}
                     (map (fn [data]
                            (let [category (:category data)
                                  d-list   (:list data)]
                              [(when (not (clojure.string/blank? category))
                                 (subheader r category paper))
                               (map #(item r %) d-list)]))
                          list))))

(rum/defc search < rum/reactive
  [r]
  (rum/adapt-class TextField {:variant     "outlined"
                              :fullWidth   true
                              :margin      "none"
                              :placeholder "Search"}))

(defn Search-box
  [r]
  {:search (rum/with-key (search r) "search")
   :table  (rum/with-key (table r) "table")})
