(ns app.renderer.forms.search.project-search
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            ["@material-ui/core/styles" :refer [styled]]
            ["@material-ui/core" :refer [Paper
                                         Box
                                         ListSubheader
                                         List
                                         ListItem
                                         ListItemText
                                         TextField]]))

(def search-atom (atom ""))

(defn title-includes?
  [base str]
  (clojure.string/includes? (clojure.string/upper-case base)
                            (clojure.string/upper-case str)))

(defn filter-list [list criteria]
  (if (clojure.string/blank? criteria)
    list
    (let [pred-proj     #(or (title-includes? (:title %) criteria)
                             (and (:code %)
                                  (title-includes? (:code %) criteria)))
          pred-category #(not (empty? (filter pred-proj (:list %))))
          f-category    (filter pred-category list)]
      (for [category f-category]
        (assoc category :list (filter pred-proj (:list category)))))))

(defn to-box [child]
  (rum/adapt-class Box {:p     1
                        :width "70%"} child))

(defn paper [child]
  (rum/adapt-class Paper {:elevation 3}
                   child))

(rum/defc item < rum/reactive
  [r data]
  (let [title (:title data)
        id    (:id data)]
    (rum/adapt-class ListItem {:button true}
                     (rum/adapt-class ListItemText
                                      {:primary title}))))

(rum/defc subheader < rum/reactive
  [r category]
  (let [theme (rum/react (citrus/subscription r [:home :theme]))]
    (rum/adapt-class ((styled  ListSubheader)
                      #(clj->js {:backgroundColor (-> theme :palette :background :paper)})) {}
                     category)))

(rum/defc table < rum/reactive
  [r]
  (let [list        (rum/react (citrus/subscription r [:project :left]))
        search      (rum/react search-atom)
        result-list (filter-list list search)]
    (rum/adapt-class List {:component "div"
                           :className "search-list"}
                     (map (fn [data]
                            (let [category (:category data)
                                  d-list   (:list data)]
                              [(when (not (clojure.string/blank? category))
                                 (subheader r category))
                               (map #(item r %) d-list)]))
                          result-list))))

(rum/defc search < rum/reactive
  [r]
  (rum/adapt-class TextField
                   {:variant     "outlined"
                    :onChange    #(reset! search-atom (.. % -target -value))
                    :fullWidth   true
                    :placeholder "Search"}))

(rum/defc Search-box
  [r]
  (-> [(rum/with-key (search r) "project-search")
       (rum/with-key (table r) "project-table")]
      paper
      to-box))
