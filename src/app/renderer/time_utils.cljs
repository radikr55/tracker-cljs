(ns app.renderer.time-utils
  (:require [cljs-time.core :as t]
            [cljs-time.format :as f]
            [cljs-time.coerce :as c]
            [goog.string :as gstring]
            [goog.string.format]
            [citrus.core :as citrus]))

(defn date->calendar-date [date]
  (f/unparse (f/formatter "d.MM.YYYY") date))

(defn days [date day]
  (t/plus- (t/first-day-of-the-month date) (t/days day)))

(defn month->calendar [date]
  (->> (range (t/number-of-days-in-the-month date))
       (map #(days date %))
       (reduce #(let [week  (t/week-number-of-year %2)
                      exist (get %1 week)]
                  (assoc %1 week
                         (conj (or exist []) %2))) {})))

(defn date->calendar-field [date]
  (f/unparse (f/formatter "EEEE, MMMM d, YYYY") date))

(defn date->calendar-title [date]
  (f/unparse (f/formatter "MMMM, YYYY") date))

(defn to-time->field [date]
  (f/unparse (f/formatter "HH:mm") date))

(defn field->to-time [date]
  (f/parse (f/formatter "HH:mm") date))

(defn get-interval [start end]
  (t/in-minutes (t/interval start end)))

(defn merge-date-time [date time]
  (t/date-time (t/year date)
               (t/month date)
               (t/day date)
               (t/hour time)
               (t/minute time)))

(defn date-only [date]
  (t/date-time (t/year date)
               (t/month date)
               (t/day date)))

(defn eq-by-date [left right]
  (t/= (date-only (t/to-default-time-zone left))
       (date-only (t/to-default-time-zone right))))

(defn to-local [date-str]
  (let [format (f/formatter-local "yyyy-MM-dd HH:mm:ss")
        resp   (t/to-default-time-zone (c/from-string date-str))]
    (f/parse format (f/unparse format resp))))

(defn get-local-without-offset [date]
  (let [format (f/formatter-local "yyyy-MM-dd HH:mm:ss")
        resp   (t/to-default-time-zone date)]
    (f/parse format (f/unparse format resp))))

(defn format-time [time]
  (let [hour    (/ time 60)
        minutes (rem time 60)]
    (if time (gstring/format "%02dh %02dm" hour minutes)
        "00h 00m")))

(defn to-interval->field [str]
  (let [start (f/parse (f/formatter "HH:mm") "00:00")
        end   (f/parse (f/formatter "HH:mm") str)]
    (get-interval start end)))

(defn format-time->field [time]
  (let [hour    (/ time 60)
        minutes (rem time 60)]
    (if time (gstring/format "%02d:%02d" hour minutes)
        "00:00")))

(defn wheel->time-field [r e date value field store]
  (let [delta   (.-deltaY e)
        fun-val (if (> delta 0)
                  #(t/minus- % (t/minutes 1))
                  #(t/plus- % (t/minutes 1)))]
    (citrus/dispatch! r store field
                      (merge-date-time date (fun-val value)))))

(defn wheel->interval-field [r e value field store]
  (let [delta   (.-deltaY e)
        new-val (if (> delta 0)
                  (dec value)
                  (inc value))
        new-val (if (> 0 new-val) 0 new-val)]
    (citrus/dispatch! r store field new-val)))

