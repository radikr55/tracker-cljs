(ns app.renderer.forms.search.issue-search
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [app.renderer.utils :refer [tc]]
            [clojure.string :as string]))

(rum/defc item < rum/reactive
  {:key-fn (fn [_ data] (:id data))}
  [r data]
  (let [title (:title data)
        code  (:code data)]
    (tc {:component :list-item
         :opts      {:button    true
                     :onClick   #(do (citrus/dispatch! r :chart :new-current-task code)
                                     (citrus/dispatch! r :router :push :home))
                     :className "table-row"}
         :child     {:component :list-item-text
                     :child     [{:component :typography
                                  :opts      {:className "search-item search-item-code"
                                              :key       "code"}
                                  :child     code}
                                 {:component :typography
                                  :opts      {:className "search-item search-item-title"
                                              :key       "title"}
                                  :child     title}]}})))

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
  (let [{list :right} (rum/react (citrus/subscription r [:project]))
        theme (rum/react (citrus/subscription r [:theme :cljs]))
        paper (-> theme :palette :background :paper)]
    (tc {:component :box
         :opts      {:className "search-list-body"}
         :child     {:component :scrollbars
                     :opts      {:autoHeight true
                                 :autoHeightMin    "calc(100vh - 140px)"
                                 :autoHeightMax    "calc(100vh - 140px)"
                                 :renderThumbVertical (fn [_]
                                                        (tc {:component :div
                                                             :opts {:className "thumb-vertical"}}))}
                     :child     (map (fn [data]
                                       (let [category (:category data)
                                             d-list   (:list data)]
                                         [(when (not (string/blank? category))
                                            (subheader r category paper))
                                          (map #(item r %) d-list)]))
                                     list)}})))

(rum/defc search < rum/reactive
  {:key-fn (fn [_] "search")}
  [r]
  (tc {:component :text-field
       :opts      {:variant     "outlined"
                   :fullWidth   true
                   :className   "search-field"
                   :margin      "none"
                   :placeholder "Task"
                   :onChange    #(citrus/dispatch! r :project :get-tasks (.. % -target -value))
                   :InputProps  {:startAdornment
                                 (tc {:component :input-adornment
                                      :opts      {:position "start"}
                                      :child     {:component :search}})}}}))

(rum/defc Search-box
  [r]
  (tc {:component :box
       :child     [(search r)
                   (table r)]}))
