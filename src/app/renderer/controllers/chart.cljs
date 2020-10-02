(ns app.renderer.controllers.chart
  (:require [app.renderer.effects :as effects]
            [cljs-time.core :as t]
            [cljs.tools.reader.edn :as edn]
            [citrus.core :as citrus]
            [cljs-time.format :as f]
            [cljs-time.coerce :as c]
            [app.renderer.time-utils :as tu]))

(def initial-state
  {:chart     [{:code nil}]
   :desc      []
   :list      []
   :current   "WELKIN-76"
   :submitted nil
   :tracked   nil
   :logged    nil
   :date      (t/date-time 2020 07 15)})

(defn calc-interval [origin]
  (mapv (fn [origin]
          (let [{:keys [start end]} origin]
            (assoc origin :interval
                   (tu/get-interval start end))))
        origin))

(defn time-merge [chart]
  (->> chart
       (group-by :code)
       (map (fn [[key val]]
              (assoc {} :code key :interval (reduce + (map :interval val)))))))

(defn format-time [row]
  (assoc row
         :format (tu/format-time (:interval row))
         :format-field (* 60  (:interval row))))

(defn away-on-top [a b]
  (cond
    (empty? (:code a)) -1
    (empty? (:code b)) 1
    :else              0))

(defn add-stubs [[key origin]]
  (let [start     (:start (first origin))
        end       (:end (last origin))
        start-day (t/at-midnight start)
        end-day   (t/at-midnight (t/plus- end (t/days 1)))]
    (cond-> origin
      (t/after? start start-day) (->> (concat [{:start start-day
                                                :end   start}]))
      (t/before? end end-day)    (concat [{:start end
                                           :end   end-day}]))))
(defn add-middle-stubs [origin]
  (loop [origin     origin
         previously nil
         result     []]
    (let [current (first origin)]
      (cond
        (nil? origin)     result
        (nil? previously) (recur (next origin) current (conj result current))
        :else             (let [previously-end (:end previously)
                                current-start  (:start current)]
                            (if (not (= previously-end current-start))
                              (recur (next origin) current (conj result {:start previously-end
                                                                         :end   current-start}
                                                                 current))
                              (recur (next origin) current (conj result current))))))))

(defn map-by-code [origin]
  (let [code (->> origin
                  (map :code)
                  (filter #(not (nil? %)))
                  first)]
    {:code code :chart origin}))

(defn add-format-time [origin]
  (->> origin
       (map #(let [format-start    (f/unparse (f/formatter "HH:mm") (:start %))
                   format-end      (f/unparse (f/formatter "HH:mm") (:end %))
                   format-interval (tu/format-time (:interval %))]
               (assoc % :format-start format-start
                      :format-end format-end
                      :format-interval format-interval)))))

(defmulti control (fn [event] event))

(defmethod control :init []
  (let [current-task (edn/read-string (js/localStorage.getItem "current-task"))]
    {:state (assoc initial-state :current-task (:code current-task))}))

(defmethod control :set-date [_ [date] state]
  {:state (assoc state :date date)})

(defmethod control :submit-all [_ _ state]
  (let [token (effects/local-storage
                nil
                :poject
                {:method :get
                 :key    :token})
        date  (:date state)
        list  (->> (:list state)
                   (filter #(not (empty? (:code %))))
                   (map #(assoc {}
                                :issueCode (:code %)
                                :timeSpent (:format-field %)
                                :date      (c/to-string (tu/merge-date-time date "12:00"))
                                :offset    (.getTimezoneOffset (js/Date.)))))]
    {:state state
     :http  {:endpoint :submit
             :params   (assoc token :query
                              list)
             :method   :post
             :on-load  :success-submit
             :on-error :error}}))

(defmethod control :success-submit [event [args r] state]
  (citrus/dispatch! r :chart :load-track-logs args)
  state)

(defmethod control :set-current-task [_ [code] state]
  {:state         (assoc state :current-task code)
   :local-storage {:method :set
                   :data   {:code code}
                   :key    :current-task}})

(defmethod control :inc-date [_ _ state]
  {:state (assoc state :date (t/plus- (:date state) (t/days 1)))})

(defmethod control :dec-date [_ _ state]
  {:state (assoc state :date (t/minus- (:date state) (t/days 1)))})

(defmethod control :load-track-logs [_ _ state]
  (let [date      (:date state)
        start-day (t/at-midnight date)
        end-day   (t/at-midnight (t/plus- date (t/days 1)))
        token     (effects/local-storage
                    nil
                    :project
                    {:method :get
                     :key    :token})]
    {:http {:endpoint :load-track-logs
            :params   (assoc token
                             :offset (.getTimezoneOffset (js/Date.))
                             :start (c/to-string start-day)
                             :end   (c/to-string end-day))
            :method   :post
            :on-load  :success-track-log-load
            :on-error :error}}))

(defmethod control :success-track-log-load [_ [result] init-state]
  (let [arr        (->> (:data result)
                        (map #(assoc {}
                                     :start  (tu/to-local (:start_date %))
                                     :end  (tu/to-local (:end_date %))
                                     :code  (:task %)))
                        (assoc {} :chart))
        descs      (->> (:desc result)
                        :issueWithSummary
                        (map #(vector {:code      (name (first %))
                                       :desc      (:summary (second %))
                                       :link      (:link (second %))
                                       :submitted (:timeSpent (second %))}))
                        flatten)
        state      (->> arr
                        :chart
                        (group-by :code)
                        (map add-stubs)
                        (map add-middle-stubs)
                        (map calc-interval)
                        (map add-format-time)
                        (map map-by-code))
        state-list (->> arr
                        :chart
                        calc-interval
                        (filter #(not (nil? (:code %))))
                        time-merge
                        (sort away-on-top)
                        (map format-time))
        submitted  (-> result :desc :timeSpent (/ 60))
        tracked    (->> state-list
                        (filter #(not (empty? (:code %))))
                        (map :format-field)
                        (reduce + 0))
        logged     (->> state-list
                        (map :format-field)
                        (reduce + 0))]
    {:state (assoc init-state
                   :chart state
                   :list state-list
                   :desc descs
                   :submitted submitted
                   :tracked (/ tracked 60)
                   :logged (/ logged 60))}))

