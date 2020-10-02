(ns app.main.api
  (:require ["axios" :as axios]
            [promesa.core :as p]))

(def url "http://localhost:3000/")

(defmulti ->endpoint (fn [id] id))

(defmethod ->endpoint :save-ping [_ _]
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

(defn fetch [{:keys [endpoint params data method headers]}]
  (-> (axios (clj->js {:method  (->method method)
                       :baseURL url
                       :url     (->endpoint endpoint)
                       :params  params
                       :data    data}))
      (p/then #(parse-body %))
      (p/catch #(print %))))

(comment
  (-> (fetch {:method   :POST
              :data     {:task  121233
                         :start (t/time-now)
                         :end   (t/time-noe)}
              :endpoint :save-ping})
      (p/then #(print %))))
