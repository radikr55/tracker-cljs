(ns app.main.api
  (:require ["axios" :as axios]
            [cljs.pprint :refer [pprint]]
            [promesa.core :as p]))

(def url "http://localhost:3000/")
;; (def error-send (atom false))

;; (add-watch error-send :reload
;;            (fn [_ _ old new]
;;              (cond
;;                (and old (not new)) (.reload @w/main-window)
;;                (and (not old) new) (send-ipc @w/main-window "offline" nil))))

(defmulti ->endpoint (fn [id] id))

(defmethod ->endpoint :save-ping [_ _]
  "/save-ping")

(defmethod ->endpoint :ping [_ _]
  "/ping")

(defn- parse-body [res]
  (-> res
      (js->clj :keywordize-keys true)))

(defn- ->json [params]
  (.stringify js/JSON (clj->js params)))

(defn- type->header [type]
  (case type
    :text {"Content-Type" "text/plain"}
    {"Content-Type" "application/json"}))

(defn- ->method [method]
  (case method
    :POST "post"
    "get"))

(defn fetch [{:keys [endpoint params data method]}]
  (let [param (clj->js {:method  (->method method)
                        :baseURL url
                        :url     (->endpoint endpoint)
                        :params  params
                        :data    data})]
    (pprint param)
    (-> (axios param)
        (p/then #(parse-body %))
        (p/catch #(print %)))))

(comment
  (-> (fetch {:method   :POST
              :data     {:task  121233
                         :start (t/time-now)
                         :end   (t/time-noe)}
              :endpoint :save-ping})
      (p/then #(print %))))
