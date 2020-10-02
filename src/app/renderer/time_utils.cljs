(ns app.renderer.time-utils
  (:require [cljs-time.core :as t]
            [cljs-time.format :as f]
            [cljs-time.coerce :as c]
            [goog.string :as gstring]
            [goog.string.format]))


(defn get-interval [start end]
  (t/in-minutes (t/interval start end)))

(defn merge-date-time [date time]
  (let [sdate (f/parse (f/formatter "HH:mm") time)]
    (t/date-time (t/year date)
                 (t/month date)
                 (t/day date)
                 (t/hour sdate)
                 (t/minute sdate))))

(defn to-local [date-str]
  (let [format (f/formatter-local "yyyy-MM-dd HH:mm:ss")
        resp   (t/to-default-time-zone (c/from-string date-str))]
    (f/parse format (f/unparse format resp))))

(defn format-time [time]
  (let [hour    (/ time 60)
        minutes (rem time 60)]
    (if time (gstring/format "%02dh %02dm" hour minutes)
        "00h 00m")))

(defn to-time->field [date]
  (f/unparse (f/formatter "HH:mm") date))

(defn to-interval->field [str]
  (let [start (f/parse (f/formatter "HH:mm") "00:00")
        end   (f/parse (f/formatter "HH:mm") str)]
    (get-interval start end)))

(defn format-time->field [time]
  (let [hour    (/ time 60)
        minutes (rem time 60)]
    (if time (gstring/format "%02d:%02d" hour minutes)
        "00:00")))
