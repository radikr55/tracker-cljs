(ns app.renderer.api
  (:require [httpurr.client.xhr :as xhr]
            [httpurr.status :as status]
            [app.renderer.utils :as u]
            [promesa.core :as p]))

(defmulti ->endpoint (fn [id] id))

(defmethod ->endpoint :ping []
  "ping")

(defmethod ->endpoint :auth-link []
  "auth-link")

(defmethod ->endpoint :user-name []
  "user-name")

(defmethod ->endpoint :oauth []
  "oauth")

(defmethod ->endpoint :active-task []
  "active-task")

(defmethod ->endpoint :project []
  "project")

(defmethod ->endpoint :tasks []
  "tasks")

(defmethod ->endpoint :submit []
  "submit")

(defmethod ->endpoint :submit-force []
  "submit-force")

(defmethod ->endpoint :load-track-logs []
  "track-logs")

(defmethod ->endpoint :save-ping []
  "save-ping")

(defmethod ->endpoint :load-stat []
  "load-stat")

(defmethod ->endpoint :by-project-id []
  "by-project-id")

(defn- ->uri [path]
  (str (u/package-config "server-link") path))

(defn- parse-body [res]
  (-> res
      js/JSON.parse
      (js->clj :keywordize-keys true)))

(defn- ->json [params]
  (.stringify js/JSON (clj->js params)))

(defn- ->xhr [uri xhr-fn params r]
  (-> uri
      (xhr-fn params)
      (p/then (fn [{status :status body :body headers :headers :as response}]
                (js/localStorage.setItem "app-version" (get headers "x-app-version"))
                (js/localStorage.setItem "app-version-link" (get headers "x-app-version-link"))
                (condp = status
                  status/ok (p/resolved (parse-body body))
                  (p/rejected (parse-body body)))))
      (p/catch (fn [e]
                 (p/rejected {:status status/service-unavailable})))))

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

(defn fetch [{:keys [reconciler endpoint params slug method type headers token]}]
  (let [xhr-fn     (method->xhr-fn method)
        xhr-params {:query-params (when-not (contains? #{:post :put :patch :delete} method) params)
                    :body         (when (contains? #{:post :put :patch :delete} method) (->json params))
                    :headers      (merge headers (type->header type) (token->header token))}]
    (-> (->endpoint endpoint slug)
        ->uri
        (->xhr xhr-fn xhr-params reconciler))))
