(ns app.renderer.forms.about
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [app.renderer.utils :refer [tc]]))

(rum/defc image < {:key-fn (fn [_] "image")}
  []
  [:img {:src    "img/icon@2x.png"
         :width  100
         :height 100}])

(rum/defc copyright < {:key-fn (fn [_] "copyright")}
  []
  [:div {:class "about-copyright"}
   [:span (str "© Softarex " (.getFullYear (js/Date.)))]])

(rum/defc dialog-about < rum/reactive [r]
  (tc {:component :dialog
       :opts      {:open      (rum/react (citrus/subscription r [:home :about]))
                   :fullWidth true
                   :maxWidth  "xs"
                   :onClose   #(citrus/subscription r :home :close-about)}
       :child     [{:component :dialog-title
                    :opts      {:key       "dialog-title"
                                :className "about-dialog-header"}
                    :child     [{:component :typography
                                 :styl      {:fontSize "25px"}
                                 :opts      {:key "about"}
                                 :child     "About"}
                                {:component :icon-button
                                 :styl      {:float   "right"
                                             :padding "0"}
                                 :opts      {:key     "dialog-cancel"
                                             :onClick #(citrus/dispatch! r :home :close-about)}
                                 :child     {:component :cancel}}]}
                   {:component :dialog-content
                    :opts      {:key "dialog-conent"}
                    :child     [{:component :box
                                 :opts      {:className "about-dialog-content"
                                             :key       "content"
                                             :pb        3}
                                 :child     [{:component :box
                                              :opts      {:key "content"}
                                              :child     [{:component :typography
                                                           :styl      {:fontSize "25px"}
                                                           :opts      {:key "title"}
                                                           :child     "TaskTracker v2"}
                                                          {:component :typography
                                                           :styl      {:fontSize      "18px"
                                                                       :paddingBottom "10px"}
                                                           :opts      {:key "tagline"}
                                                           :child     "«We want peace and love»"}
                                                          {:component :typography
                                                           :styl      {:fontFamily "Roboto"
                                                                       :fontSize   "12px"}
                                                           :opts      {:key "dev1"}
                                                           :child     "Made by a developer who is tired"}
                                                          {:component :typography
                                                           :styl      {:fontFamily "Roboto"
                                                                       :fontSize   "12px"}
                                                           :opts      {:key "dev2"}
                                                           :child     "of fixing bugs on the old version"}]}

                                             (image)]}
                                (copyright)]}]}))
