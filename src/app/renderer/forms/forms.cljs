(ns app.renderer.forms.forms
  (:require [rum.core :as rum]))


;; (rum/defc InputField
;;   [{:keys [placeholder type value on-blur on-change events]}]
;;   (let [input-container (or container InputFieldContainer)
;;         input-events    (merge {:on-change #(on-change (.. % -target -value))
;;                                 :on-blur   on-blur
;;                                 :on-focus  on-focus}
;;                                events)]
;;     (input-container
;;       value
;;       [:input.form-control.form-control-lg
;;        (into
;;          {:placeholder placeholder
;;           :type        (or type :text)
;;           :value       value}
;;          input-events)]
;;       (when errors
;;         (InputErrors errors)))))


