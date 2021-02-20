(ns app.main.api
  (:require [httpurr.client.node :as c]
            [httpurr.status :as status]
            [cljs.pprint :refer [pprint]]
            [app.main.utils :refer [send-ipc]]
            [app.main.window :as w]
            [promesa.core :as p]
            [app.main.local-storage :as ls]))

(def url (ls/package-config "server-link"))

(defmulti ->endpoint (fn [id] id))

(defmethod ->endpoint :ping []
  "ping")

(defmethod ->endpoint :save-ping []
  "save-ping")

(defn- ->uri [path]
  (str url path))

(defn- parse-body [res]
  (-> res
      js/JSON.parse
      (js->clj :keywordize-keys true)))

(defn- ->json [params]
  (.stringify js/JSON (clj->js params)))

(defn- ->node [uri node-fn params]
  (-> uri
      (node-fn params)
      (p/then (fn [{status :status body :body :as response}]
                (condp = status
                  status/ok           (p/resolved (parse-body body))
                  status/unauthorized (p/rejected (send-ipc @w/main-window "force-logout" nil))
                  (p/rejected (parse-body body)))))))

(defn- method->node-fn [method]
  (case method
    :post c/post
    :put c/put
    :patch c/patch
    :delete c/delete
    c/get))

(defn- type->header [type]
  (case type
    :text {"Content-Type" "text/plain"}
    {"Content-Type" "application/json"}))

(defn- token->header [token]
  (if token
    {"Authorization" (str "Token " token)}
    {}))

(defn fetch [{:keys [endpoint params method type headers token]}]
  (let [node-fn     (method->node-fn method)
        node-params {:query-params (when-not (contains? #{:post :put :patch :delete} method) params)
                     :body         (when (contains? #{:post :put :patch :delete} method) (->json params))
                     :headers      (merge headers (type->header type) (token->header token))}]
    (-> (->endpoint endpoint)
        ->uri
        (->node node-fn node-params))))

