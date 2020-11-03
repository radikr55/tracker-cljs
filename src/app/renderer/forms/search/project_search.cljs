(ns app.renderer.forms.search.project-search
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc]]
            [citrus.core :as citrus]
            ["@material-ui/core/styles" :refer [styled]]
            ["@material-ui/core" :refer [Paper
                                         Devider
                                         Box
                                         ListSubheader
                                         List
                                         ListItem
                                         ListItemText
                                         TextField]]))

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

(rum/defc item < rum/reactive
  {:key-fn (fn [r data] (str (:id  data)))}
  [r data]
  (let [title (:title data)
        id    (:id data)]
    (tc {:component :list-item
         :opts      {:button    true
                     :key       (str id)
                     :onClick   #(citrus/dispatch! r :project :get-by-project-id id)
                     :className "table-row"}
         :child     {:component :list-item-text
                     :child     {:component :typography
                                 :opts      {:className "search-item search-item-title"
                                             :key       "title"}
                                 :child     title}}})))

(rum/defc subheader < rum/reactive
  {:key-fn (fn [_ id _] id)}
  [r category paper]
  (tc {:component :list-subheader
       :opts      {:className "search-list-subheader"}
       :styl      {:backgroundColor paper}
       :child     category}))

(rum/defc table < rum/reactive
  {:key-fn (fn [_] "table")}
  [r]
  (let [list   (rum/react (citrus/subscription r [:project :left]))
        theme  (rum/react (citrus/subscription r [:theme :cljs]))
        paper  (-> theme :palette :background :paper)
        search (rum/react (citrus/subscription r [:project :search-project]))
        f-list (filter-list list search)]
    (tc {:component :box
         :opts      {:pt 1}
         :child     {:component :paper
                     :opts      {:elevation 3}
                     :child     {:component :list
                                 :opts      {:key       "project"
                                             :className "search-list"}
                                 :child     (map (fn [data]
                                                   (let [category (:category data)
                                                         d-list   (:list data)]
                                                     [(when (not (clojure.string/blank? category))
                                                        (subheader r category paper))
                                                      (map #(item r %) d-list)]))
                                                 f-list)}}})))

(rum/defc search < rum/reactive
  {:key-fn (fn [_] "search")}
  [r]
  (tc {:component :text-field
       :opts      {:onChange    #(citrus/dispatch! r :project :set-search-project (.. % -target -value))
                   :fullWidth   true
                   :variant     "outlined"
                   :className   "search-field"
                   :margin      "none"
                   :placeholder "Project"
                   :InputProps  {:startAdornment
                                 (tc {:component :input-adornment
                                      :opts      {:position "start"}
                                      :child     {:component :search}})}}}))

(rum/defc Search-box
  [r]
  (tc  {:component :box
        :child     [(search r)
                    (table r)]}))
