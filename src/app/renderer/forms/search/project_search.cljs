(ns app.renderer.forms.search.project-search
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc]]
            [citrus.core :as citrus]
            [clojure.string :as s]))

(def selected-project (atom nil))

(defn title-includes?
  [base str]
  (s/includes? (s/upper-case base)
                            (s/upper-case str)))


(defn filter-list [list criteria]
  (if (s/blank? criteria)
    list
    (let [pred-proj     #(or (title-includes? (:title %) criteria)
                             (= (:id %) @selected-project)
                             (and (:code %)
                                  (title-includes? (:code %) criteria)))
          pred-category #(seq (filter pred-proj (:list %)))
          f-category    (filter pred-category list)]
      (for [category f-category]
        (assoc category :list (filter pred-proj (:list category)))))))

(rum/defc item < rum/reactive
                 {:key-fn (fn [_ data] (str (:id data)))}
  [r data]
  (let [title       (:title data)
        id          (:id data)
        selected-id (rum/react selected-project)
        class       (cond-> "table-row"
                            (= selected-id id) (str " selected-project "))]
    (tc {:component :list-item
         :opts      {:button    true
                     :key       (str id)
                     :onClick   #(do (reset! selected-project id)
                                     (citrus/dispatch! r :project :get-by-project-id id))
                     :className class}
         :child     {:component :list-item-text
                     :child     {:component :typography
                                 :opts      {:className "search-item search-item-title"
                                             :key       "title"}
                                 :child     title}}})))

(rum/defc subheader < rum/reactive
                      {:key-fn (fn [_ id _] id)}
  [_ category paper]
  (tc {:component :list-item
       :opts      {:className "search-list-subheader"
                   :disabled true}
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
         :opts      {:className "search-list-body"}
         :child  {:component :scrollbars
                     :opts      {:autoHeight true
                                 :autoHeightMin    "calc(100vh - 140px)"
                                 :autoHeightMax    "calc(100vh - 140px)"
                                 :renderThumbVertical (fn [opts]
                                                        (print (js->clj opts))
                                                        (tc {:component :div
                                                             :opts {:className "thumb-vertical"}}))}
                     :child     (map (fn [data]
                                       (let [category (:category data)
                                             d-list   (:list data)]
                                         [(when (not (s/blank? category))
                                            (subheader r category paper))
                                          (map #(item r %) d-list)]))
                                     f-list)}   })))

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
  (tc {:component :box
       :child     [(search r)
                   (table r)]}))
