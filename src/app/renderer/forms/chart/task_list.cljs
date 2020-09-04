(ns app.renderer.forms.chart.task-list
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc on-wheel-vertical]]
            [app.renderer.forms.chart.submenu :as submenu]
            [citrus.core :as citrus]))

(def left-list-ref (js/React.createRef))

(rum/defc header
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

(rum/defc footer
  [h-header]
  (tc {:component :box
       :opts      {:height    (str h-header "px")
                   :key       "footer"
                   :className "top-border"
                   :width     "100%"}}))

(defn open-menu [r event code]
  (citrus/dispatch! r :home :open-submenu
                    {:position-submenu {:mouseX (.-clientX event)
                                        :mouseY (.-clientY event)}
                     :submenu-code     code}))

(rum/defc item < rum/reactive
  {:key-fn (fn [_ row] (str (:code row)))}
  [r row h-body]
  (let [code           (:code row)
        desc           (:desc row)
        class          "task-box"
        highlight-code (rum/react (citrus/subscription r [:home :submenu-code]))
        class          (if (= code highlight-code)
                         (str "highlight-task " class)
                         class)]
    (tc {:component :box
         :opts      {:height        (str h-body "px")
                     :onContextMenu #(open-menu r % code)
                     :className     class}
         :child     [{:component :box
                      :opts      {:key "code"}
                      :child     {:component :typography
                                  :styl      {:min-inline-size "fit-content"
                                              :font-size       "12px"
                                              :font-weight     "900"}
                                  :child     code}}
                     {:component :box
                      :opts      {:key "desc"}
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
         :child     (for [row list]
                      (item r row h-body))})   ))

(rum/defc TaskList < rum/reactive
  [r h-top h-header h-body]
  (vector (header r h-header)
          (body r h-top h-header h-body)
          (footer h-header)
          (submenu/SubMenu r)))
