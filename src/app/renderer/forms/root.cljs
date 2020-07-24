(ns app.renderer.forms.root
  (:require [rum.core :as rum]
            [app.renderer.forms.login :refer [Login]]
            [app.renderer.forms.header :refer [Header]]))

(rum/defc Root < rum/reactive
  [r]
  [:div
   (Header r)
   (Login r)
   ])
