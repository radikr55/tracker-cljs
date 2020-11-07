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
  {:chart     [{:code nil}]
   :desc      []
   :list      []
   :activity  []
   :current   "WELKIN-76"
   :submitted nil
   :tracked   nil
   :logged    nil
   :date      (t/date-time 2020 10 30)})

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

(defmethod control :success-submit [event [args r] state]
  (citrus/dispatch! r :chart :load-track-logs args)
  state)

(defmethod control :set-current-task [_ [code] state]
  {:state         (assoc state :current-task code)
   :local-storage {:method :set
                   :data   {:code code}
                   :key    :current-task}})

(defmethod control :new-current-task [_ [code] state]
  (let [token (effects/local-storage
                nil
                :poject
                {:method :get
                 :key    :token})]
    {:http {:endpoint :active-task
            :params   (assoc token :query
                             {:code code})
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
    (print (:list state) )
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
            :on-error :error}}
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
        update-local (when (contains? exist codes)
                       {:local-storage {:method :set
                                        :data   {:code ""}
                                        :key    :current-task}})]
    (citrus/dispatch! r :chart :load-track-logs)
    (citrus/dispatch! r :task-popper :close-popper)
    (merge {:state (assoc state :current-task "")} update-local)))

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
  (let [date       (:date init-state)
        descs      (->> (:desc result)
                        :issueWithSummary
                        (map #(vector {:code      (name (first %))
                                       :desc      (:summary (second %))
                                       :link      (:link (second %))
                                       :submitted (:timeSpent (second %))}))
                        flatten)
        arr        (->> (:data result)
                        (map #(assoc {}
                                     :start  (tu/to-local (:start_date %))
                                     :inactive-log (:inactive_log %)
                                     :end  (tu/to-local (:end_date %))
                                     :code  (:task %)))
                        (add-mock date "" #(empty? (:code %)))
                        (add-empty-tasks date descs))
        activity   (->>  arr
                         (activity-arr)
                         (map add-stubs)
                         (map add-middle-stubs)
                         (map calc-interval)
                         (map add-format-time))
        state      (->> arr
                        (group-by-code)
                        (map merge-nearby)
                        (map add-stubs)
                        (map add-middle-stubs)
                        (map calc-interval)
                        (map add-format-time)
                        (map map-by-code))
        state-list (->> arr
                        calc-interval
                        (filter #(not (nil? (:code %))))
                        time-merge
                        (sort-by :code)
                        (sort away-on-top)
                        (map format-time))
        submitted  (-> result :desc :timeSpent (/ 60))
        tracked    (->> state-list
                        (filter #(seq %))
                        (filter #(seq (:code %)))
                        (map :format-field)
                        (reduce + 0))
        logged     (->> state-list
                        (map :format-field)
                        (reduce + 0))]
    {:state (assoc init-state
                   :chart state
                   :activity activity
                   :list state-list
                   :desc descs
                   :submitted submitted
                   :tracked (/ tracked 60)
                   :logged (/ logged 60))}))

(comment
  (control :success-track-log-load [init] initial-state)

  (def init {:data
             [{:jira_ticket  nil,
               :task         "WELKIN-76",
               :created_date nil,
               :end_date     "2020-10-30T23:42:00Z",
               :start_date   "2020-10-30T22:01:00Z",
               :id           1057411,
               :log_length   101,
               :user_id      166,
               :client_id    nil,
               :uuid         "b74311d8-f972-4625-b0c4-22eec55232c9",
               :inactive_log false,
               :tag_id       nil,
               :updated_date nil}
              {:jira_ticket  nil,
               :task         "WELKIN-76",
               :created_date nil,
               :end_date     "2020-10-30T23:47:00Z",
               :start_date   "2020-10-30T23:42:00Z",
               :id           1057412,
               :log_length   5,
               :user_id      166,
               :client_id    nil,
               :uuid         "47ddccd8-e675-4246-b3b8-18739b7d6c39",
               :inactive_log true,
               :tag_id       nil,
               :updated_date nil}
              {:jira_ticket  nil,
               :task         "WELKIN-76",
               :created_date nil,
               :end_date     "2020-10-30T23:49:00Z",
               :start_date   "2020-10-30T23:47:00Z",
               :id           1057413,
               :log_length   2,
               :user_id      166,
               :client_id    nil,
               :uuid         "ae1fe65f-e34f-4366-b957-980b8bb03b63",
               :inactive_log false,
               :tag_id       nil,
               :updated_date nil}
              {:jira_ticket  nil,
               :task         "WELKIN-76",
               :created_date nil,
               :end_date     "2020-10-31T00:00:00Z",
               :start_date   "2020-10-30T23:49:00Z",
               :id           1057414,
               :log_length   11,
               :user_id      166,
               :client_id    nil,
               :uuid         "7764344a-3b4a-4ad4-88ab-934707627a9c",
               :inactive_log true,
               :tag_id       nil,
               :updated_date nil}
              {:jira_ticket  nil,
               :task         "WELKIN-76",
               :created_date nil,
               :end_date     "2020-10-31T00:01:00Z",
               :start_date   "2020-10-31T00:00:00Z",
               :id           1057415,
               :log_length   1,
               :user_id      166,
               :client_id    nil,
               :uuid         "de46df20-a930-4e35-bd90-36530394b596",
               :inactive_log false,
               :tag_id       nil,
               :updated_date nil}
              {:jira_ticket  nil,
               :task         "WELKIN-76",
               :created_date nil,
               :end_date     "2020-10-31T00:10:00Z",
               :start_date   "2020-10-31T00:01:00Z",
               :id           1057416,
               :log_length   9,
               :user_id      166,
               :client_id    nil,
               :uuid         "25826e6c-6ede-4dde-8857-43c8584a8ef7",
               :inactive_log true,
               :tag_id       nil,
               :updated_date nil}
              {:jira_ticket  nil,
               :task         "WELKIN-76",
               :created_date nil,
               :end_date     "2020-10-31T00:31:00Z",
               :start_date   "2020-10-31T00:10:00Z",
               :id           1057417,
               :log_length   21,
               :user_id      166,
               :client_id    nil,
               :uuid         "fa46f45d-ea2b-468f-a7f7-85781023cf19",
               :inactive_log false,
               :tag_id       nil,
               :updated_date nil}
              {:jira_ticket  nil,
               :task         "WELKIN-76",
               :created_date nil,
               :end_date     "2020-10-31T00:41:00Z",
               :start_date   "2020-10-31T00:31:00Z",
               :id           1057418,
               :log_length   10,
               :user_id      166,
               :client_id    nil,
               :uuid         "a20dc187-adb1-4860-870c-41450fdc06dc",
               :inactive_log true,
               :tag_id       nil,
               :updated_date nil}
              {:jira_ticket  nil,
               :task         "WELKIN-76",
               :created_date nil,
               :end_date     "2020-10-31T00:44:00Z",
               :start_date   "2020-10-31T00:41:00Z",
               :id           1057419,
               :log_length   3,
               :user_id      166,
               :client_id    nil,
               :uuid         "519f273d-038c-4b0e-acaf-1add35733cff",
               :inactive_log false,
               :tag_id       nil,
               :updated_date nil}
              {:jira_ticket  nil,
               :task         "WELKIN-76",
               :created_date nil,
               :end_date     "2020-10-31T00:53:00Z",
               :start_date   "2020-10-31T00:44:00Z",
               :id           1057420,
               :log_length   9,
               :user_id      166,
               :client_id    nil,
               :uuid         "004b3682-5d8b-4986-89b0-1fd38b07f7ec",
               :inactive_log true,
               :tag_id       nil,
               :updated_date nil}
              {:jira_ticket  nil,
               :task         "WELKIN-76",
               :created_date nil,
               :end_date     "2020-10-31T00:55:00Z",
               :start_date   "2020-10-31T00:53:00Z",
               :id           1057421,
               :log_length   2,
               :user_id      166,
               :client_id    nil,
               :uuid         "cd3ffba9-bd19-45b3-9fcf-1ee057c09139",
               :inactive_log false,
               :tag_id       nil,
               :updated_date nil}
              {:jira_ticket  nil,
               :task         "WELKIN-42",
               :created_date nil,
               :end_date     "2020-10-31T10:14:00Z",
               :start_date   "2020-10-31T09:58:00Z",
               :id           1057422,
               :log_length   16,
               :user_id      166,
               :client_id    nil,
               :uuid         "8f4d0df3-638e-4443-a6c6-96d67e3720d9",
               :inactive_log false,
               :tag_id       nil,
               :updated_date nil}
              {:jira_ticket  nil,
               :task         "WELKIN-42",
               :created_date nil,
               :end_date     "2020-10-31T10:19:00Z",
               :start_date   "2020-10-31T10:14:00Z",
               :id           1057448,
               :log_length   5,
               :user_id      166,
               :client_id    nil,
               :uuid         "3929ba95-8884-4f0f-903a-ab2249236412",
               :inactive_log true,
               :tag_id       nil,
               :updated_date nil}
              {:jira_ticket  nil,
               :task         "WELKIN-42",
               :created_date nil,
               :end_date     "2020-10-31T10:29:00Z",
               :start_date   "2020-10-31T10:19:00Z",
               :id           1057424,
               :log_length   10,
               :user_id      166,
               :client_id    nil,
               :uuid         "12f74f69-30de-4015-bd3e-e06ed0896120",
               :inactive_log false,
               :tag_id       nil,
               :updated_date nil}
              {:jira_ticket  nil,
               :task         "WELKIN-76",
               :created_date nil,
               :end_date     "2020-10-31T11:29:00Z",
               :start_date   "2020-10-31T10:29:00Z",
               :id           1057458,
               :log_length   60,
               :user_id      166,
               :client_id    nil,
               :uuid         "0872045a-f1a0-4ff0-a54c-249bd7438d29",
               :inactive_log true,
               :tag_id       nil,
               :updated_date nil}],
             :desc
             {:issueWithSummary
              {:WELKIN-42
               {:issueCode "WELKIN-42",
                :summary
                "WDZ-429: Assessment URL can still be referenced as a variable and be sent to patients",
                :timeSpent 0,
                :link      "http://localhost:2990/jira/browse/WELKIN-42"},
               :WELKIN-9
               {:issueCode "WELKIN-9",
                :summary
                "WDZ-272: Add ability to activate/deactivate assessments in workshop",
                :timeSpent 9720,
                :link      "http://localhost:2990/jira/browse/WELKIN-9"},
               :WELKIN-76
               {:issueCode "WELKIN-76",
                :summary
                "WDZ-548: Publish Notification events for SMS subscriptions",
                :timeSpent 10440,
                :link      "http://localhost:2990/jira/browse/WELKIN-76"}},
              :timeSpent        20160,
              :notSubmittedDate []}}))

