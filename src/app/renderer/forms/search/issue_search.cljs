(ns app.renderer.forms.search.issue-search
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [app.renderer.utils :refer [tc]]
            ["@material-ui/core/styles" :refer [styled]]
            ["@material-ui/core" :refer [Paper
                                         ListSubheader
                                         Box
                                         List
                                         ListItem
                                         ListItemText
                                         TextField]]))

(def search-atom (atom ""))

(rum/defc item < rum/reactive
  {:key-fn (fn [_ data] (:id data))}
  [r data]
  (let [title (:title data)
        id    (:id data)]
    (tc {:component :list-item
         :opts      {:button    true
                     :className "table-row"}
         :child     {:component :list-item-text
                     :opts      {:secondary title}}})))

(rum/defc subheader < rum/reactive
  {:key-fn (fn [_ id _] id)}
  [r category paper]
  (tc {:component :list-subheader
       :styl      {:backgroundColor paper}
       :child     category}))

(rum/defc table < rum/reactive
  {:key-fn (fn [_] "table")}
  [r]
  (let [{list :right} (rum/react (citrus/subscription r [:project]))
        theme         (rum/react (citrus/subscription r [:theme :cljs]))
        paper         (-> theme :palette :background :paper)
        search        (rum/react search-atom)]
    (tc {:component :box
         :opts      {:pt 1}
         :child     {:component :paper
                     :opts      {:elevation 3}
                     :child     {:component :list
                                 :opts      {:component "nav"
                                             :key       "project"
                                             :className "search-list"}
                                 :child     (map (fn [data]
                                                   (let [category (:category data)
                                                         d-list   (:list data)]
                                                     [(when (not (clojure.string/blank? category))
                                                        (subheader r category paper))
                                                      (map #(item r %) d-list)]))
                                                 list)}}})))

(rum/defc search < rum/reactive
  {:key-fn (fn [_] "search")}
  [r]
  (tc {:component :text-field
       :opts      {:variant     "outlined"
                   :fullWidth   true
                   :className   "search-field"
                   :margin      "none"
                   :label       "Search"
                   :placeholder "Task"
                   :InputProps  {:startAdornment
                                 (tc {:component :input-adornment
                                      :opts      {:position "start"}
                                      :child     {:component :search}})}}}))

(rum/defc Search-box
  [r]
  (tc  {:component :box
        :child     [(search r)
                    (table r)]}))
