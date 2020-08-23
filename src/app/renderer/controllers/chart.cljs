(ns app.renderer.controllers.chart
  (:require [cljs-time.core :as t]))

(defn get-interval [start end]
  (t/in-minutes (t/interval start end)))

(def initial-state {:chart    [{:start (t/today-at 5 59)
                                :end   (t/today-at 6 00)
                                :code  "WELKIN-123"}
                               {:start (t/today-at 6 00)
                                :end   (t/today-at 8 00)
                                :code  "WELKIN-124"}
                               {:start (t/today-at 8 00)
                                :end   (t/today-at 10 00)
                                :code  "WELKIN-123"}
                               {:start (t/today-at 10 00)
                                :end   (t/today-at 12 00)
                                :code  "Away"}
                               {:start (t/today-at 12 00)
                                :end   (t/today-at 13 00)
                                :code  "WELKIN-125"}
                               {:start (t/today-at 13 00)
                                :end   (t/today-at 14 00)
                                :code  "WELKIN-124"}]
                    :desc     [{:code "WELKIN-123" :desc "dec task"}
                               {:code "WELKIN-124" :desc "dec task 124"}
                               {:code "WELKIN-125" :desc "[L3] [Dev] [Risalto] Behavior of Assessment Responses endpoint"}]
                    :order    nil
                    :current  "WELKIN-123"
                    :selected #{"WELKIN-123"}})

(defn add-stubs [origin]
  (let [start     (:start (first origin))
        end       (:end (last origin))
        start-day (t/at-midnight start)
        end-day   (t/at-midnight (t/plus- end (t/days 1)))]
    (cond-> origin
      (t/after? start start-day) (->> (concat [{:start start-day
                                                :end   start}]))
      (t/before? end end-day)    (concat [{:start end
                                           :end   end-day}]))))

(defn calc-interval [origin]
  (mapv (fn [origin]
          (let [{:keys [start end]} origin]
            (assoc origin :interval
                   (get-interval start end))))
        origin))

(defn time-merge [chart]
  (->> chart
       (group-by :code)
       (map (fn [[key val]]
              (assoc {} :code key :interval (reduce + (map :interval val)))))))

(defn desc-merge [state chart]
  (for [c chart]
    (assoc c :desc
           (->> state
                (filter #(= (:code %) (:code c)))
                first
                :desc))))

(let [state      (->> initial-state
                      :chart
                      add-stubs
                      calc-interval)
      state-list (->> state
                      (filter #(not (nil? (:code %))))
                      (filter #(not (= (:code %) "Away")))
                      time-merge
                      (desc-merge (:desc initial-state)))]
  state-list)

(defmulti control (fn [event] event))

(defmethod control :init []
  (let [state      (->> initial-state
                        :chart
                        add-stubs
                        calc-interval)
        state-list (->> state
                        (filter #(not (nil? (:code %))))
                        (filter #(not (= (:code %) "Away")))
                        time-merge
                        (desc-merge (:desc initial-state)))]
    {:state (assoc initial-state :chart state :list state-list)}))

(defmethod control :switch-all-selected [_ [val] state]
  (let [selected (:selected state)
        exist?   (every? selected val)]
    {:state (assoc state :selected (if exist? #{} val))}))

(defmethod control :switch-selected [_ [val] state]
  (let [selected (:selected state)
        exist?   (contains? selected val)]
    {:state (assoc state :selected
                   (if exist?
                     (disj selected val)
                     (conj selected val)))}))

(defmethod control :set-current-task [_ [task-code] state]
  {:state (assoc state :current task-code)})

(defmethod control :switch-order [_ _ state]
(let [order (:order state)]
  (cond
    (= :asc order)  {:state (assoc state
                                   :list (sort-by :interval (:list state))
                                   :order :desc)}
    (= :desc order) {:state (assoc state
                                   :list (sort-by :interval #(compare %2 %1) (:list state))
                                   :order nil)}
    :else           {:state (assoc state
                                   :list (sort-by :code (:list state))
                                   :order :asc)})))

(defmethod control :load [_ _ state]
{:state state})

