(ns app.renderer.effects
  (:require [citrus.core :as citrus]
            [app.renderer.api :as api]
            [cljs.tools.reader.edn :as edn]
            [promesa.core :as p]
            ["electron" :refer [ipcRenderer]]))

(defn local-storage [r controller-name effect]
  (let [{:keys [method data key on-read]} effect]
    (case method
      :set    (js/localStorage.setItem (name key) data)
      :remove (js/localStorage.removeItem (name key))
      :get    (edn/read-string (js/localStorage.getItem (name key)))
      nil)))

(defmulti dispatch! (fn [_ _ effect]
                      (type effect)))

(defmethod dispatch! Keyword [r c event & args]
  (apply citrus/dispatch! r c event args))

(defmethod dispatch! PersistentArrayMap [r c effects & oargs]
  (doseq [[effect [c event & args]] effects]
    (apply dispatch! r c event (concat args oargs))))

(defn http [r c {:keys [endpoint params slug on-load on-error method type headers token]}]
  (citrus/dispatch! r :loading :on)
  (->
    (api/fetch {:endpoint endpoint
                :params   params
                :slug     slug
                :method   method
                :type     type
                :headers  headers
                :token    token})
    (p/then (fn [e]
              (citrus/dispatch! r :loading :off)
              (dispatch! r c on-load e r)))
    (p/catch (fn [e]
               (citrus/dispatch! r :loading :off)
               (dispatch! r c on-error e)))))

(defn ipc-renderer [r c {:keys [type args]}]
  (.send ipcRenderer type args))

;; (defmethod ipc-renderer false [{:keys [type fun]}]
;;   (.on ipcRenderer type fun))
;; (ipc-renderer {:send? true} {:type "oauth"
;;                              :args "123"})
