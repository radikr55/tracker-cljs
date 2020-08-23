(ns app.renderer.forms.home.tasks.list
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc format-time]]
            [citrus.core :as citrus]))

(rum/defc task-time < rum/reactive
  [r task-code]
  (let [chart    (rum/react (citrus/subscription r [:chart :chart]))
        selected (rum/react (citrus/subscription r [:chart :selected]))
        time     (->> chart
                      (filter #(not (nil? (:code %))))
                      (filter #(= task-code (:code %)))
                      (map :interval)
                      (reduce + 0))]
    (tc {:component :button-base
         :opts      {:disabled true}
         :child     (format-time time)})))

(defn delete-row
  [delete? color]
  {:component :list-item-secondary-action
   :child     [{:component :icon-button
                :opts      {:key     "confirm"
                            :color   "secondary"
                            :onClick #(reset! delete? false)}
                :child     {:component :cancel}}
               {:component :icon-button
                :opts      {:key     "cancel"
                            ;; :color   "primary"
                            :onClick #(reset! delete? false)}
                :child     {:component :check
                            :opts      {:style {:color color}}}}
               ]})

(rum/defcs item < rum/reactive
  (rum/local false ::delete)
  {:key-fn (fn [_ data] (:code data))}
  [state r data]
  (let [desc       (:desc data)
        code       (:code data)
        delete?    (::delete state)
        theme      (rum/react (citrus/subscription r [:theme :cljs]))
        selected   (rum/react (citrus/subscription r [:chart :selected]))
        visibility (contains? selected code)]
    (tc {:component :list-item
         :opts      {:button-base (not @delete?)
                     :button      true
                     :className   "table-row"}
         :child     [{:component :list-item-text
                      :styl      (when @delete? {:filter "blur(2px)"})
                      :opts      {:primary   code
                                  :onClick   #(citrus/dispatch! r :chart :set-current-task code)
                                  :secondary desc}}
                     (if @delete?
                       (delete-row delete? (-> theme :palette :success :main))
                       {:component :list-item-secondary-action
                        :child     [(task-time r code)
                                    {:component :icon-button
                                     :opts      {:key     "delete"
                                                 :onClick #(reset! delete? true)}
                                     :child     {:component :delete-forever-outlined}}
                                    {:component :icon-button
                                     :opts      {:onClick #(citrus/dispatch! r :chart :switch-selected code)
                                                 :key     "eye"}
                                     :child     {:component (if visibility
                                                              :visibility
                                                              :visibility-outlined)}}]})]})))

(rum/defc away < rum/reactive
  [r]
  (let [all        (rum/react (citrus/subscription r [:chart :chart]))
        selected   (rum/react (citrus/subscription r [:chart :selected]))
        code       "Away"
        visibility (contains? selected code)]
    (tc {:component :list-item
         :opts      {:button true}
         :child     [{:component :list-item-text
                      :styl      {:color "orange"}
                      :opts      {:primary   code
                                  :key       "away"
                                  :onClick   #(citrus/dispatch! r :chart :set-current-task code)
                                  :secondary ""}}
                     {:component :list-item-secondary-action
                      :child     [(task-time r code)
                                  {:component :box
                                   :opts      {:width   "48px"
                                               :key     "space"
                                               :display "inline-flex"}}
                                  {:component :icon-button
                                   :opts      {:onClick #(citrus/dispatch! r :chart :switch-selected "Away")
                                               :key     "eye"}
                                   :child     {:component (if visibility
                                                            :visibility
                                                            :visibility-outlined)}}]}]})))

(rum/defc list-item < rum/reactive
  [r]
  (let [tasks (rum/react (citrus/subscription r [:chart :list]))]
    [(away r)
     (map #(item r %) tasks)]))

(defn Tasks-list
  [r]
  {:component :list
   :opts      {:className "tasks-list"}
   :child     (list-item r)})
