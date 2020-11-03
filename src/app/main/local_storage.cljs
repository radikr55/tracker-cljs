(ns app.main.local-storage
  (:require [promesa.core :as p]
            [goog.string :as gstring]
            [cljs.tools.reader.edn :as edn]
            [goog.string.format]))

(defn local-set [web-content item value]
  (-> (.executeJavaScript web-content (gstring/format "localStorage.setItem('%s','%s')" item value) true)
      (p/catch #(print %))))

(defn local-get [web-content item]
  (-> (.executeJavaScript web-content (gstring/format "localStorage.getItem('%s')" item) true)
      (p/then #(edn/read-string %))
      (p/catch #(print  %))))

(defn local-remove [web-content item]
  (-> (.executeJavaScript web-content (gstring/format "localStorage.removeItem('%s')" item) true)
      (p/catch #(print  %))))
