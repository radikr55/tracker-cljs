(ns app.renderer.api
  (:require [httpurr.client.xhr :as xhr]
            [httpurr.status :as status]
            [promesa.core :as p]))

(defmulti ->endpoint (fn [id] id))

(defmethod ->endpoint :comment [_ [article-id comment-id]]
  (str "articles/" article-id "/comments/" comment-id))

(defmethod ->endpoint :auth-link [_ _]
  "auth-link")

(defmethod ->endpoint :user-name [_ _]
  "user-name")

(defmethod ->endpoint :oauth [_ _]
  "oauth")

(defmethod ->endpoint :active-task [_ _]
  "active-task")

(defmethod ->endpoint :project [_ _]
  "project")

(defmethod ->endpoint :tasks [_ _]
  "tasks")

(defmethod ->endpoint :submit [_ _]
  "submit")

(defmethod ->endpoint :load-track-logs [_ _]
  "track-logs")

(defmethod ->endpoint :save-ping [_ _]
  "ping")

(defmethod ->endpoint :load-stat [_ _]
  "load-stat")

(defmethod ->endpoint :by-project-id [_ _]
  "by-project-id")

(defn- ->uri [path]
  (str "http://localhost:3000/" path))

(defn- parse-body [res]
  (-> res
      js/JSON.parse
      (js->clj :keywordize-keys true)))

(defn- ->json [params]
  (.stringify js/JSON (clj->js params)))

(defn- ->xhr [uri xhr-fn params]
  (-> uri
      (xhr-fn params)
      (p/then (fn [{status :status body :body :as response}]
                (condp = status
                  status/ok (p/resolved (parse-body body))
                  (p/rejected (parse-body body)))))))

(defn- method->xhr-fn [method]
  (case method
    :post   xhr/post
    :put    xhr/put
    :patch  xhr/patch
    :delete xhr/delete
    xhr/get))

(defn- type->header [type]
  (case type
    :text {"Content-Type" "text/plain"}
    {"Content-Type" "application/json"}))

(defn- token->header [token]
  (if token
    {"Authorization" (str "Token " token)}
    {}))

(defn fetch [{:keys [endpoint params slug method type headers token]}]
  (let [xhr-fn     (method->xhr-fn method)
        xhr-params {:query-params (when-not (contains? #{:post :put :patch :delete} method) params)
                    :body         (when (contains? #{:post :put :patch :delete} method) (->json params))
                    :headers      (merge headers (type->header type) (token->header token))}]
    (-> (->endpoint endpoint slug)
        ->uri
        (->xhr xhr-fn xhr-params))))
