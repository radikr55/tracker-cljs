(ns app.renderer.forms.chart.chart-popper
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc format-time]]
            [citrus.core :as citrus]))

(rum/defc body  < rum/reactive
  {:key-fn (fn [_] "body")}
  [r row]
  (let [start (:start row)
        end   (:end row)
        task  (:code row)]
    (tc {:component :paper
         :child     {:component :box
                     :styl      {:display "flex"}
                     :child     [{:component :typography
                                  :child     "Edit Time"}
                                 {:component :time-field
                                  :opts      {:valut "00:00"
                                              :colon ":"}} ]}})))

(rum/defc Popper < rum/reactive
{:key-fn (fn [_] "popover")}
  [r]
  (let [position (rum/react (citrus/subscription r [:home :position-submenu]))
        row      (rum/react (citrus/subscription r [:home :row-box]))
        open?    (rum/react (citrus/subscription r [:home :open-popover]))]
    (when open? (tc {:component :popover
                     :opts      {:open            open?
                                 :anchorReference "anchorPosition"
                                 :anchorPosition  {:top  (:mouseY position)
                                                   :left (:mouseX position)}
                                 :onClose         #(citrus/dispatch! r :home :close-chart-menu)}
                     :child     (body r row)}))))
