(ns app.renderer.forms.chart.left.task-list
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc on-wheel-vertical]]
            [app.renderer.forms.chart.left.task-popper :as task-popper]
            [citrus.core :as citrus]))

(def left-list-ref (js/React.createRef))
(def away "Away (Not working)")

(rum/defc header <
  {:key-fn (fn [_] "header")}
  [r h-header top?]
  (tc {:component :box
       :opts      {:height    (str h-header "px")
                   :key       "header"
                   :className "bottom-border header-task-list"
                   :display   "flex"
                   :onClick   #(citrus/dispatch! r :router :push :search)
                   :width     "100%"}
       :child     [{:component :add
                    :opts      {:key "icon"}
                    :styl      {:font-size "18px"}}
                   {:component :box
                    :opts      {:key "title"}
                    :child     {:component :typography
                                :styl      {:min-inline-size "fit-content"
                                            :font-size       "12px"
                                            :font-weight     "900"}
                                :child     "Add Task"}}]}))

(rum/defc footer <
  {:key-fn (fn [_] "footer")}
  [h-header]
  (tc {:component :box
       :opts      {:height    (str h-header "px")
                   :key       "footer"
                   :className "top-border"
                   :width     "100%"}}))

(defn open-menu [r event code link]
  (citrus/dispatch! r :task-popper :open-popper
                    {:position {:mouseX (.-clientX event)
                                :mouseY (.-clientY event)}
                     :code     code
                     :link     link}))

(rum/defc item < rum/reactive
                 {:key-fn (fn [_ row] (str (:code row)))}
  [r row h-body]
  (let [code           (:code row)
        code-str       (if (empty? code) away code)
        meta           (->> (rum/react (citrus/subscription r [:chart :desc]))
                            (filter #(= code-str (:code %)))
                            first)
        desc           (:desc meta)
        link           (:link meta)
        context-menu   (when (seq code) #(open-menu r % code link))
        highlight-code (rum/react (citrus/subscription r [:task-popper :code]))
        current-task   (rum/react (citrus/subscription r [:chart :current-task]))
        class          (cond-> "task-box"
                               (empty? code) (str " away ")
                               (seq code) (str " active ")
                               (= code highlight-code) (str " highlight-task ")
                               (= code current-task) (str " selected-task "))]
    (tc {:component :box
         :opts      {:height        (str h-body "px")
                     :onContextMenu context-menu
                     :onClick       #(citrus/dispatch! r :chart :set-current-task code)
                     :className     class}
         :child     [{:component :box
                      :opts      {:key            "code"
                                  :justifyContent "space-between"
                                  :alignItems     "center"
                                  :display        "flex"}
                      :child     [{:component :typography
                                   :opts      {:key "code"}
                                   :styl      {:min-inline-size "fit-content"
                                               :font-size       "12px"
                                               :font-weight     "900"}
                                   :child     code-str}
                                  (when (and (seq code)
                                             (= code current-task))
                                    {:component :box
                                     :opts      {:key       "badge"
                                                 :className "badge-box"}
                                     :child     {:component :typography
                                                 :opts      {:className "task-badge"}
                                                 :child     "TRACKING"}})]}
                     {:component :box
                      :opts      {:key       "desc"
                                  :className "task-desc"}
                      :child     {:component :typography
                                  :styl      {:font-size "12px"}
                                  :opts      {:noWrap true}
                                  :child     desc}}]})))

(rum/defc body < rum/reactive
                 {:key-fn (fn [_] "body")}
  [r h-top h-header h-body]
  (let [middle-list-ref (rum/react (citrus/subscription r [:home :middle-list-ref]))
        right-list-ref  (rum/react (citrus/subscription r [:home :right-list-ref]))
        list            (rum/react (citrus/subscription r [:chart :list]))]
    (citrus/dispatch! r :home :set-left-list-ref left-list-ref)
    (tc {:component :box
         :opts      {:overflow "hidden"
                     :ref      left-list-ref
                     :onWheel  #(on-wheel-vertical % [middle-list-ref
                                                      left-list-ref
                                                      right-list-ref])
                     :height   (str "calc(100vh - " (+ 2 h-top (* 2 h-header)) "px)")}
         :child     (map #(item r % h-body) list)})))

(rum/defc TaskList < rum/reactive
  [r h-top h-header h-body]
  (vector (header r h-header)
          (body r h-top h-header h-body)
          (footer h-header)
          (task-popper/SubMenu r)))
