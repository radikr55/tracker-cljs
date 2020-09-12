(ns app.renderer.controllers.chart
  (:require [cljs-time.core :as t]
            [cljs-time.format :as ft]
            [app.renderer.utils :as ut]))

(def away "Away (Not working)")

(def initial-state {:chart   [{:start (t/today-at 5 59)
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
                               :code  away}
                              {:start (t/today-at 12 00)
                               :end   (t/today-at 13 00)
                               :code  "WELKIN-125"}
                              {:start (t/today-at 15 00)
                               :end   (t/today-at 15 30)
                               :code  "WELKIN-125"}
                              {:start (t/today-at 13 00)
                               :end   (t/today-at 14 00)
                               :code  "WELKIN-124"}]
                    :desc    [{:code away}
                              {:code "WELKIN-123" :desc "dec task"}
                              {:code "WELKIN-124" :desc "dec task 124"}
                              {:code "WELKIN-125" :desc "[L3] [Dev] [Risalto] Behavior of Assessment Responses endpoint"}]
                    :current "WELKIN-123"
                    })

(defn get-interval [start end]
  (t/in-minutes (t/interval start end)))

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

(defn format-time [row]
  (assoc row :format (ut/format-time (:interval row))))

(defn away-on-top [a b]
  (cond
    (= away (:code a)) -1
    (= away (:code b)) 1
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
       (map #(let [format-start    (ft/unparse (ft/formatter "HH:mm") (:start %))
                   format-end      (ft/unparse (ft/formatter "HH:mm") (:end %))
                   format-interval (ut/format-time (:interval %))]
               (assoc % :format-start format-start
                      :format-end format-end
                      :format-interval format-interval)))))

(let [state (->> initial-state
                 :chart
                 (group-by :code)
                 (map add-stubs)
                 (map add-middle-stubs)
                 (map calc-interval)
                 (map add-format-time)
                 (map map-by-code))]
  state)

(let [state      (->> initial-state
                      :chart
                      calc-interval)
      state-list (->> state
                      (filter #(not (nil? (:code %))))
                      ;; (filter #(not (= (:code %) "Away")))
                      time-merge
                      (desc-merge (:desc initial-state))
                      (sort away-on-top)
                      (map format-time))]
  state-list)

(defmulti control (fn [event] event))

(defmethod control :init []
  {:state initial-state})

;; (defmethod control :switch-all-selected [_ [val] state]
;;   (let [selected (:selected state)
;;         exist?   (every? selected val)]
;;     {:state (assoc state :selected (if exist? #{} val))}))

;; (defmethod control :switch-selected [_ [val] state]
;;   (let [selected (:selected state)
;;         exist?   (contains? selected val)]
;;     {:state (assoc state :selected
;;                    (if exist?
;;                      (disj selected val)
;;                      (conj selected val)))}))

(defmethod control :set-current-task [_ [task-code] state]
  {:state (assoc state :current task-code)})

;; (defmethod control :switch-order [_ _ state]
;;   (let [order (:order state)]
;;     (cond
;;       (= :asc order)  {:state (assoc state
;;                                      :list (sort-by :interval (:list state))
;;                                      :order :desc)}
;;       (= :desc order) {:state (assoc state
;;                                      :list (sort-by :interval #(compare %2 %1) (:list state))
;;                                      :order nil)}
;;       :else           {:state (assoc state
;;                                      :list (sort-by :code (:list state))
;;                                      :order :asc)})))

(defmethod control :load [_ _ state]
  (let [state      (->> initial-state
                        :chart
                        (group-by :code)
                        (map add-stubs)
                        (map add-middle-stubs)
                        (map calc-interval)
                        (map add-format-time)
                        (map map-by-code))
        state-list (->> initial-state
                        :chart
                        calc-interval
                        (filter #(not (nil? (:code %))))
                        time-merge
                        (desc-merge (:desc initial-state))
                        (sort away-on-top)
                        (map format-time))]
    {:state (assoc initial-state :chart state :list state-list)}))

