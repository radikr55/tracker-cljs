(ns app.renderer.controllers.chart
  (:require [app.renderer.effects :as effects]
            [cljs-time.core :as t]
            [cljs.tools.reader.edn :as edn]
            [cljs.pprint :refer [pprint]]
            [citrus.core :as citrus]
            [cljs-time.format :as f]
            [cljs-time.coerce :as c]
            [app.renderer.time-utils :as tu]))

(def initial-state
  {:chart         [{:code nil}]
   :desc          []
   :list          []
   :activity      []
   :submitted     nil
   :tracked       nil
   :logged        nil
   :not-submitted []
   :date          (t/date-time 2020 10 30)})

(defn calc-interval [origin]
  (mapv (fn [origin]
          (let [{:keys [start end]} origin]
            (if (t/equal? start end)
              (assoc origin :interval 0)
              (assoc origin :interval
                     (tu/get-interval start end)))))
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
                            (if (not (t/= previously-end current-start))
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
  (let [current-task (edn/read-string (js/localStorage.getItem "current-task"))
        task         (if (nil? (:code current-task)) "" (:code current-task))]
    {:state (assoc initial-state :current-task task)}))

(defmethod control :set-date [_ [date] state]
  {:state (assoc state :date (tu/date-only (t/to-default-time-zone date)))})

(defmethod control :submit-all [_ _ state]
  (let [token (effects/local-storage
                nil
                :poject
                {:method :get
                 :key    :token})
        date  (:date state)
        list  (->> (:list state)
                   (filter #(seq (:code %)))
                   (map #(assoc {}
                                :issueCode (:code %)
                                :timeSpent (:format-field %)
                                :date      (c/to-string (tu/merge-date-time date
                                                                            (tu/field->to-time "12:00")))
                                :offset    (.getTimezoneOffset (js/Date.)))))]
    {:state state
     :http  {:endpoint :submit
             :params   (assoc token :query
                              list)
             :method   :post
             :on-load  :success-submit
             :on-error :error}}))

(defmethod control :clear-notification [_ _ state]
  (let [token (effects/local-storage
                nil
                :poject
                {:method :get
                 :key    :token})
        date  (:date state)
        list  (->> (:list state)
                   (filter #(seq (:code %)))
                   (map #(assoc {}
                                :issueCode (:code %)
                                :timeSpent 0
                                :date      (c/to-string (tu/merge-date-time date
                                                                            (tu/field->to-time "12:00")))
                                :offset    (.getTimezoneOffset (js/Date.)))))]
    (if (= 0 (:submitted state))
      {:state state
       :http  {:endpoint :submit-force
               :params   (assoc token :query
                                list)
               :method   :post
               :on-load  :success-submit
               :on-error :error}}
      state)))

(defmethod control :success-submit [event [args r] state]
  (citrus/dispatch! r :chart :load-track-logs args)
  state)

(defmethod control :set-current-task [_ [code] state]
  {:state         (assoc state :current-task code)
   :local-storage {:method :set
                   :data   {:code code}
                   :key    :current-task}})

(defmethod control :new-current-task [_ [code] state]
  (let [date  (:date state)
        token (effects/local-storage
                nil
                :poject
                {:method :get
                 :key    :token})]
    {:http {:endpoint :active-task
            :params   (assoc token :query
                             {:code code}
                             :date (c/to-string (tu/merge-date-time date
                                                                    (tu/field->to-time "00:00")))
                             :offset (.getTimezoneOffset (js/Date. date)))
            :method   :post
            :on-load  :success-save-task
            :on-error :error}}))

(defmethod control :delete-current-task [_ [code] state]
  (let [date      (:date state)
        start-day (t/at-midnight date)
        end-day   (t/at-midnight (t/plus- date (t/days 1)))
        token     (effects/local-storage
                    nil
                    :project
                    {:method :get
                     :key    :token})]
    {:http {:endpoint :active-task
            :params   (assoc token
                             :offset (.getTimezoneOffset (js/Date. date))
                             :tasks [code]
                             :date      (c/to-string (tu/merge-date-time date
                                                                         (tu/field->to-time "12:00")))
                             :start (c/to-string start-day)
                             :end   (c/to-string end-day))
            :method   :delete
            :on-load  :success-delete-task
            :on-error :error}}))

(defmethod control :delete-all-empty-tasks [_ _ state]
  (let [date      (:date state)
        start-day (t/at-midnight date)
        end-day   (t/at-midnight (t/plus- date (t/days 1)))
        token     (effects/local-storage
                    nil
                    :project
                    {:method :get
                     :key    :token})
        list      (->> (:list state)
                       (filter #(not (= (:code % ) "") ))
                       (filter #(= 0 (:interval %)))
                       (map :code)
                       (into #{}))]
    (if (empty? list)
      state
      {:http {:endpoint :active-task
              :params   (assoc token
                               :offset (.getTimezoneOffset (js/Date. date))
                               :tasks list
                               :date      (c/to-string (tu/merge-date-time date
                                                                           (tu/field->to-time "12:00")))
                               :start (c/to-string start-day)
                               :end   (c/to-string end-day))
              :method   :delete
              :on-load  :success-delete-task
              :on-error :error}} )
    ))

(defmethod control :success-save-task [event [args r] state]
  (let [code (:code args)]
    (citrus/dispatch! r :chart :load-track-logs)
    {:state         (assoc state :current-task code)
     :local-storage {:method :set
                     :data   {:code code}
                     :key    :current-task}}))

(defmethod control :success-delete-task [event [args r] state]
  (let [codes        (:codes args)
        exist        (-> (effects/local-storage nil
                                                :poject
                                                {:method :get
                                                 :key    :current-task})
                         :code)
        update-local (when (some #(= exist %) codes)
                       {:state         (assoc state :current-task "")
                        :local-storage {:method :set
                                        :data   {:code ""}
                                        :key    :current-task}})]
    (citrus/dispatch! r :chart :load-track-logs)
    (citrus/dispatch! r :task-popper :close-popper)
    (merge state update-local)))

(defmethod control :error [_ e state]
  (print e))

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
                             :offset (.getTimezoneOffset (js/Date. date))
                             :start (c/to-string start-day)
                             :end   (c/to-string end-day))
            :method   :post
            :on-load  :success-track-log-load
            :on-error :error}}))

(defn merge-nearby [[key list]]
  [key (loop [origin     list
              previously nil
              result     []]
         (let [current         (first origin)
               current-start   (:start current)
               current-end     (:end current)
               current-code    (:code current)
               previously-end  (:end previously)
               previously-code (:code previously)
               btlast          (into [] (butlast result))
               nearby?         (and (not (nil? current-code))
                                    (= previously-code current-code)
                                    (t/= current-start previously-end))]
           (cond
             (nil? origin) result
             nearby?       (let [el (assoc previously :end current-end)]
                             (recur (next origin) el (conj btlast el)))
             :else         (recur (next origin) current (conj result current)))))])

(defn activity-arr [arr]
  (if (not-empty arr)
    {:ativity arr}
    {}))

(defn add-mock [date code ->some? arr]
  (let [contains-away (some ->some? arr)]
    (if contains-away
      arr
      (conj arr
            {:code  code
             :start (t/at-midnight date)
             :end   (t/at-midnight date)}))))

(defn add-empty-tasks [date descs arr]
  (loop [origin descs
         result arr]
    (let [current (first origin)]
      (cond
        (nil? origin) result
        :else         (let [current-code (:code current)
                            with-mock    (add-mock date current-code #(= (:code %) current-code) result)]
                        (recur (next origin) with-mock))))))

(defn compare-date [a b]
  (let [left  (:start a)
        right (:start b)]
    (cond
      (t/before? left right) -1
      (t/after? left right)  1
      :else                  0)))

(defn group-by-code [arr]
  (let [->group (group-by :code arr)
        res     (for [[code value] ->group]
                  {code (->> arr
                             (filter #(not (nil? (:code %))))
                             (filter #(not= (:code %) code))
                             (filter #(not (t/= (:start %) (:end %))))
                             (map #(dissoc % :code))
                             (into value)
                             (sort compare-date))})]
    (into {} res)))

(defmethod control :success-track-log-load [event [result] init-state]
  (let [date          (:date init-state)
        not-submitted (->> (-> result :desc :notSubmittedDate)
                           (map #(c/from-long %)))
        descs         (->> (:desc result)
                           :issueWithSummary
                           (map #(vector {:code      (name (first %))
                                          :desc      (:summary (second %))
                                          :link      (:link (second %))
                                          :submitted (:timeSpent (second %))}))
                           flatten)
        arr           (->> (:data result)
                           (map #(assoc {}
                                        :start  (tu/to-local (:start_date %))
                                        :inactive-log (:inactive_log %)
                                        :end  (tu/to-local (:end_date %))
                                        :code  (:task %)))
                           (add-mock date "" #(empty? (:code %)))
                           (add-empty-tasks date descs))
        activity      (->>  arr
                            (activity-arr)
                            (map add-stubs)
                            (map add-middle-stubs)
                            (map calc-interval)
                            (map add-format-time))
        state         (->> arr
                           (group-by-code)
                           (map merge-nearby)
                           (map add-stubs)
                           (map add-middle-stubs)
                           (map calc-interval)
                           (map add-format-time)
                           (map map-by-code))
        state-list    (->> arr
                           calc-interval
                           (filter #(not (nil? (:code %))))
                           time-merge
                           (sort-by :code)
                           (sort away-on-top)
                           (map format-time))
        submitted     (-> result :desc :timeSpent (/ 60))
        tracked       (->> state-list
                           (filter #(seq %))
                           (filter #(seq (:code %)))
                           (map :format-field)
                           (reduce + 0))
        logged        (->> state-list
                           (map :format-field)
                           (reduce + 0))]
    (print not-submitted)
    {:state (assoc init-state
                   :chart state
                   :activity activity
                   :list state-list
                   :desc descs
                   :submitted submitted
                   :not-submitted not-submitted
                   :tracked (/ tracked 60)
                   :logged (/ logged 60))}))

