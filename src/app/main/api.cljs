(ns app.main.api
  (:require ["axios" :as axios]
            [cljs.pprint :refer [pprint]]
            [promesa.core :as p]
            [app.main.local-storage :as ls]))

(def url (ls/package-config "server-link"))

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
