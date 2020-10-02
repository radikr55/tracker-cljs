(ns app.renderer.controllers.project
  (:require [app.renderer.effects :as effects]))

(def initial-state {
                    :search-project nil
                    :search-task    nil})

(defmulti control (fn [event] event))

(defmethod control :init [_ [reconciler]]
  {:state initial-state})

(defmethod control :get-by-project-id [_ [project-id] state]
  (let [token (effects/local-storage
                nil
                :project
                {:method :get
                 :key    :token})]
    {:http {:endpoint :by-project-id
            :params   (assoc token :query {:key project-id})
            :method   :post
            :on-load  (if project-id :success-tasks :success-by-project-id)
            :on-error :error}}))

(defmethod control :get-projects [_ [_] state]
  (let [token (effects/local-storage
                nil
                :project
                {:method :get
                 :key    :token})]
    {:http {:endpoint :project
            :params   token
            :method   :post
            :on-load  :success-projects
            :on-error :error}}))

(defmethod control :get-tasks [_ [task] state]
  (let [token (effects/local-storage
                nil
                :poject
                {:method :get
                 :key    :token})]
    {:http {:endpoint :tasks
            :params   (assoc token :query {:key task})
            :method   :post
            :on-load  :success-tasks
            :on-error :error}}))

(defmethod control :success-projects [event [args] state]
  {:state args})

(defmethod control :success-tasks [event [args] state]
{:state (assoc state :right args)})

(defmethod control :success-by-project-id [event [args] state]
{:state (assoc state :right (:right args))})

(defmethod control :set-search-project [_ [key] state]
{:state (assoc state :search-project key)})

(defmethod control :set-search-task [_ [key] state]
{:state (assoc state :search-task key)})

(defmethod control :error [_ [error] state]
(print error))
