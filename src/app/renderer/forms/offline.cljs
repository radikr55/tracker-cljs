(ns app.renderer.forms.offline
  (:require [rum.core :as rum]
            [app.renderer.utils :refer [tc]]))

(rum/defc Offline []
  (tc {:component :grid
       :styl      {:width  "100%"
                   :height "100vh"}
       :opts      {:container  true
                   :spacing    2
                   :direction  "row"
                   :justify    "center"
                   :alignItems "center"
                   }
       :child     [{:component :grid
                    :opts      {:item true
                                :key  "text"
                                :xs   2}
                    :child     {:component :typography
                                :styl      {:fontSize "25px"}
                                :child     "Offline"}}
                   {:component :grid
                    :opts      {:xs   2
                                :key  "progress"
                                :item true}
                    :child     {:component :circular-progress}}]}))
