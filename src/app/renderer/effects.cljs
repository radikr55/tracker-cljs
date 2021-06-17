(ns app.renderer.effects
  (:require [citrus.core :as citrus]
            [app.renderer.api :as api]
            [cljs.tools.reader.edn :as edn]
            [promesa.core :as p]
            ["electron" :refer [ipcRenderer]]))

(defn local-storage [_ _ effect]
  (let [{:keys [method data key _]} effect]
    (case method
      :set (js/localStorage.setItem (name key) data)
      :remove (js/localStorage.removeItem (name key))
      :get (edn/read-string (js/localStorage.getItem (name key)))
      :add (js/localStorage.setItem (name key)
                                    (merge
                                      data
                                      (edn/read-string (js/localStorage.getItem (name key)))))
      nil)))

(defmulti dispatch! (fn [_ _ effect]
                      (type effect)))

(defmethod dispatch! Keyword [r c event & args]
  (apply citrus/dispatch! r c event args))

(defmethod dispatch! PersistentArrayMap [r c effects & oargs]
  (doseq [[_ [c event & args]] effects]
    (apply dispatch! r c event (concat args oargs))))

(defn http [r c {:keys [endpoint params slug on-load on-error method type headers token]}]
  (citrus/dispatch! r :loading :on)
  (->
    (api/fetch {:reconciler r
                :endpoint   endpoint
                :params     params
                :slug       slug
                :method     method
                :type       type
                :headers    headers
                :token      token})
    (p/then (fn [e]
              (citrus/dispatch! r :error :check-version e)
              e))
    (p/then (fn [e]
              (citrus/dispatch! r :loading :off)
              (dispatch! r c on-load e r)
              e))
    (p/catch (fn [e]
               (citrus/dispatch! r :loading :off)
               (citrus/dispatch! r :error :show-error e r)
               (dispatch! r c on-error e r)))))

(defn ipc-renderer [r c {:keys [type args]}]
  (.send ipcRenderer type args))
