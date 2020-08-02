(ns app.renderer.forms.search.issue-search
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            ["@material-ui/core" :refer [Paper
                                         ListSubheader
                                         Box
                                         List
                                         ListItem
                                         ListItemText
                                         TextField]]))

(def search-atom (atom ""))

(defn to-box [child]
  (rum/adapt-class Box {:p 1 :width "100%"} child))

(defn paper [child]
  (rum/adapt-class Paper {:elevation 3}
                   child))

(rum/defc list-item < rum/reactive
  [r title]
  (rum/adapt-class ListItem {:button true}
                   (rum/adapt-class ListItemText
                                    {:primary (str "test" title)})))
(rum/defc item < rum/reactive
  [r data]
  (let [title (:title data)
        id    (:id data)]
    (rum/adapt-class ListItem {:button true}
                     (rum/adapt-class ListItemText
                                      {:primary title}))))

(rum/defc subheader < rum/reactive
  [r category]
  (rum/adapt-class ListSubheader
                   category))

(rum/defc table < rum/reactive
  [r]
  (let [{list :right} (rum/react (citrus/subscription r [:project]))
        search        (rum/react search-atom)]
    (rum/adapt-class List {:component "nav"
                           :className "search-list"}
                     (map (fn [data]
                            (let [category (:category data)
                                  d-list   (:list data)]
                              [(when (not (clojure.string/blank? category))
                                 (subheader r category))
                               (map #(item r %) d-list)]))
                          list))))

(rum/defc search < rum/reactive
  [r]
  (rum/adapt-class TextField {:variant     "outlined"
                              :fullWidth   true
                              :margin      "none"
                              :placeholder "Search"}))

(rum/defc Search-box
  [r]
  (-> [(search r)
       (table r)]
      paper
      to-box))
