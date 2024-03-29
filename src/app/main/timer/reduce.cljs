(ns app.main.timer.reduce
  (:require [cljs-time.core :as t]
            [cljs-time.coerce :as c]
            [app.main.window :as w]
            [app.main.timer.notification :as n]
            [app.main.api :as api]
            [app.main.utils :refer [send-ipc]]
            [app.main.local-storage :as ls]
            [promesa.core :as p]
            [cljs-time.format :as ft]))

(def inactive-interval 5)
(def time-send (* 60 inactive-interval))                    ; sec
(def time-check 5)                                          ; sec
(def format "yyyy/MM/dd HH:mm")
(def send-interval (atom nil))
(def check-interval (atom nil))
(def last-send-log (atom nil))
(def last-inactive (atom nil))

(defn get-interval [start end]
  (t/in-minutes (t/interval start end)))

(defn set-top [coll x]
  (conj (pop coll) x))

(defn to-date [str]
  (ft/parse (ft/formatter format) str))

(defn interval-less? [ping-package]
  (< (get-interval (:start ping-package) (:end ping-package)) inactive-interval))

(defn merge-packages
  "Merge packages if they are in sequence (left.end + 1) = right.start"
  [left right]
  (let [left-last (last left)
        near?     (and (= (:status left-last) (:status right))
                       (t/= (:end left-last) (:start right)))]
    (cond
      (nil? left-last) [right]
      near? (set-top left (assoc left-last :end (:end right)))
      :else (conj left right))))

(defn set-inactive-log
  "Set inactive log boolean by status"
  [ping-package]
  (assoc ping-package :inactive (= (:status ping-package) "inactive")))

(defn set-inactive-task
  "Set inactive log task empty if status=inactive"
  [ping-package]
  (if (= (:status ping-package) "inactive")
    (assoc ping-package :task "")
    ping-package))

(defn inactive->active
  "Switch incative status to active if less then 5"
  [ping-package]
  (if (and (not (:last? ping-package))
           (interval-less? ping-package))
    (assoc ping-package :status "active")
    ping-package))

(defn fill-packages
  "Fill ping packages in group by active status"
  [result current]
  (let [last-res       (last result)
        last-end       (:end last-res)
        current-status (:status (last current))
        current-task   (:task (last current))
        current-time   (to-date (first current))
        status?        (= (:status last-res) current-status)
        near?          (t/= (t/plus- last-end (t/minutes 1)) current-time)]
    (cond
      (nil? last-res) (conj result {:start  current-time
                                    :end    current-time
                                    :status current-status
                                    :task   current-task})
      (and near? status?) (set-top result (assoc last-res :end current-time))
      near? (conj result {:start  last-end
                          :end    current-time
                          :status current-status
                          :task   current-task})
      :else (conj result {:start  current-time
                          :end    current-time
                          :status current-status
                          :task   current-task}))))

(defn set-last-to-inactive
  [ping-package]
  (set-top ping-package (assoc (last ping-package) :last? true)))

(defn collect-package [map-ping]
  (loop [origin map-ping
         result []]
    (if (nil? origin)
      result
      (let [current (first origin)]
        (cond
          (empty? result) (recur (next origin) [{:start  (t/minus- (to-date (first current)) (t/minutes 1))
                                                 :end    (to-date (first current))
                                                 :status (:status (last current))
                                                 :task   (:task (last current))}])
          :else (recur (next origin) (fill-packages result current)))))))

(defn process-ping []
  (when @w/main-window
    (let [web-content (.-webContents @w/main-window)]
      (-> (ls/local-get web-content "time")
          (p/then #(->> (into [] %)
                        (sort-by first)))
          (p/then (fn [ping-vector]
                    (when (seq ping-vector)
                      (->> ping-vector
                           (collect-package)
                           (set-last-to-inactive)
                           (map inactive->active)
                           (map set-inactive-log)
                           (reduce #(merge-packages %1 %2) [])
                           (map set-inactive-task)
                           (map #(assoc %
                                   :start (c/to-string (:start %))
                                   :end (c/to-string (:end %))))))))
          (p/catch #(print "test" %))))))

(defn validate-package [packages]
  (let [last-item      (last packages)
        last-inactive? (:inactive last-item)
        start          (:start last-item)
        end            (:end last-item)
        size           (count packages)]
    (and (seq packages)
         (or (not= 1 size) (not (t/= start end)))
         (not last-inactive?))))

(defn fix-packages
  "add previously end time if p.end + 1 = c.start"
  [packages]
  (let [last-send-log-end (:end @last-send-log)
        last-end-date     (c/to-date-time last-send-log-end)
        c-first           (first packages)
        c-start           (c/to-date-time (:start c-first))]
    (if (and last-send-log-end
             (t/= (t/plus- last-end-date (t/minutes 1)) c-start))
      (cons (assoc c-first :start last-send-log-end)
            (drop 1 packages))
      packages)))

(defn send-ping [packages]
  (when @w/main-window
    (let [web-content (.-webContents @w/main-window)
          offset      (.getTimezoneOffset (js/Date.))
          ->packages  (fix-packages packages)
          send-fetch  (fn [token] (-> (api/fetch
                                        {:method   :post
                                         :params   (assoc token
                                                     :data ->packages
                                                     :offset offset)
                                         :endpoint :save-ping})
                                      (p/then #(ls/local-remove web-content "time"))
                                      (p/then #(send-ipc @w/main-window "refresh" nil))
                                      (p/catch #(print "error send-ping" %))))]
      (-> (ls/local-get web-content "token")
          (p/then (fn [token]
                    (when (and
                            (validate-package ->packages)
                            token)
                      (-> (ls/local-get web-content "token")
                          (p/then send-fetch)
                          (p/then #(reset! last-send-log (last ->packages)))
                          (p/catch #(print %))))))
          (p/catch #(print %))))))

(defn inactive-show [package]
  (when (seq package)
    (let [element           (->> package
                                 (into [])
                                 pop
                                 last)
          last-inactive-end (:end @last-inactive)]
      (when (and element
                 (:inactive element)
                 (not (= last-inactive-end (:end element))))
        (reset! last-inactive element)
        (reset! n/inactive-interval (get-interval (c/to-date (:start element))
                                                  (c/to-date (:end element))))
        (send-ping package)))))

(defn send-fun []
  (-> (process-ping)
      (p/then #(send-ping %))
      (p/catch #(print "timer-send-ping " %)))
  (js/clearTimeout @send-interval)
  (reset! check-interval
          (js/setTimeout send-fun (* time-send 1000))))

(defonce timer-send-ping
         (reset! send-interval
                 (js/setTimeout send-fun (* time-send 1000))))

(defn check-fun []
  (-> (process-ping)
      (p/then #(inactive-show %))
      (p/catch #(print "check-inactive " %)))
  (reset! check-interval
          (js/setTimeout check-fun (* time-check 1000))))

(defonce check-inactive
         (reset! check-interval
                 (js/setTimeout check-fun (* time-check 1000))))



;; (-> (process-ping)
;;     (p/then #(send-ping %))
;;     (p/catch #(print "timer-send-ping " %)))

;; (-> (->> (into [] {"2021/04/25 19:28" {:status "active", :task "WELKIN-9"}, "2021/04/25 19:56" {:status "inactive", :task "WELKIN-9"}, "2021/04/25 19:42" {:status "active", :task "WELKIN-9"}, "2021/04/25 19:58" {:status "inactive", :task "WELKIN-9"}, "2021/04/25 19:34" {:status "inactive", :task "WELKIN-9"}, "2021/04/25 19:33" {:status "inactive", :task "WELKIN-9"}, "2021/04/25 19:32" {:status "inactive", :task "WELKIN-9"}, "2021/04/25 20:03" {:status "active", :task "WELKIN-9"}, "2021/04/25 19:50" {:status "inactive", :task "WELKIN-9"}, "2021/04/25 19:30" {:status "inactive", :task "WELKIN-9"}, "2021/04/25 19:41" {:status "active", :task "WELKIN-9"}, "2021/04/25 19:29" {:status "inactive", :task "WELKIN-9"}, "2021/04/25 19:52" {:status "active", :task "WELKIN-9"}, "2021/04/25 19:26" {:status "active", :task "WELKIN-9"}, "2021/04/25 19:51" {:status "active", :task "WELKIN-9"}, "2021/04/25 19:59" {:status "inactive", :task "WELKIN-9"}, "2021/04/25 19:47" {:status "inactive", :task "WELKIN-9"}, "2021/04/25 19:37" {:status "active", :task "WELKIN-9"}, "2021/04/25 19:27" {:status "active", :task "WELKIN-9"}, "2021/04/25 19:31" {:status "inactive", :task "WELKIN-9"}, "2021/04/25 19:36" {:status "inactive", :task "WELKIN-9"}, "2021/04/25 19:39" {:status "active", :task "WELKIN-9"}, "2021/04/25 19:54" {:status "inactive", :task "WELKIN-9"}, "2021/04/25 19:43" {:status "active", :task "WELKIN-9"}, "2021/04/25 19:48" {:status "inactive", :task "WELKIN-9"}, "2021/04/25 19:38" {:status "active", :task "WELKIN-9"}, "2021/04/25 19:35" {:status "inactive", :task "WELKIN-9"}, "2021/04/25 20:01" {:status "inactive", :task "WELKIN-9"}, "2021/04/25 19:57" {:status "inactive", :task "WELKIN-9"}, "2021/04/25 20:02" {:status "inactive", :task "WELKIN-9"}, "2021/04/25 19:46" {:status "inactive", :task "WELKIN-9"}, "2021/04/25 19:53" {:status "inactive", :task "WELKIN-9"}, "2021/04/25 19:24" {:status "inactive", :task "WELKIN-9"}, "2021/04/25 20:00" {:status "inactive", :task "WELKIN-9"}, "2021/04/25 19:45" {:status "active", :task "WELKIN-9"}, "2021/04/25 19:49" {:status "inactive", :task "WELKIN-9"}, "2021/04/25 19:44" {:status "active", :task "WELKIN-9"}, "2021/04/25 19:25" {:status "active", :task "WELKIN-9"}, "2021/04/25 19:40" {:status "active", :task "WELKIN-9"}, "2021/04/25 19:55" {:status "inactive", :task "WELKIN-9"}})
;;          (sort-by first))
;;     ping->vector
;;     pprint)


;(comment
;  (-> (process-ping)
;      (p/then #(send-ping %))
;      (p/catch #(print "timer-send-ping " %)))
;
;  (-> (process-ping)
;      (p/then print))
;
;  ;(-> (send-ping '({:start    2021-02-09T14:29:00.000Z
;  ;                  :end      2021-02-09T14:49:00.000Z
;  ;                  :status   "active"
;  ;                  :task     "WELKIN-46",
;  ;                  :inactive false}))
;  ;    (p/then print))
;  (-> (api/fetch
;        {:method   :post
;         :params   {:name   "r.shylo"
;                    :token  "JbMjR3MNho0ms9Rv8DtU7gRyeOGwnCfi"
;                    :secret "M6jnwvmefmG9PlSFWFf1ThSDy9y0aDj7"}
;         :endpoint :save-ping})
;      (p/then js->clj)
;      (p/then print)
;      (p/catch #(print (js->clj %)))
;      )
;
;  (-> (api/fetch
;        {:method   :get
;         :endpoint :sa})
;      (p/then js->clj)
;      (p/then print)
;      (p/catch #(print (js->clj %)))
;      )
;
;  (js/clearInterval @send-interval)
;
;  (js/clearInterval @check-interval)
;
;  (reset! n/inactive-interval 5)
;
;  (-> (process-ping)
;      ;; (p/then pprint)
;      (p/then #(send-ping %))
;      (p/catch #(print %)))
;
;; (defn ping->vector [ping-vector]
;;   (->> (collect-package ping-vector)
;;        (map #(inactive->active %))
;;        (map #(set-inactive-log %))
;;        (reduce #(merge-packages %1 %2) [])
;;        (map #(assoc %
;;                     :start (c/to-string (:start %))
;;                     :end (c/to-string (:end %))))))

;
;  {"2020/11/08 02:23" {:status "active", :task "SA_TT-33"}, "2020/11/08 02:24" {:status "active", :task "SA_TT-33"}, "2020/11/08 02:25" {:status "active", :task "SA_TT-33"}, "2020/11/08 02:26" {:status "active", :task "SA_TT-33"}, "2020/11/08 02:27" {:status "active", :task "SA_TT-33"}, "2020/11/08 02:28" {:status "active", :task "SA_TT-33"}}

;
;  (def init (into {} [["2020/10/31 12:04" {:status "inactive", :task "WELKIN-76"}]
;                      ["2020/10/31 12:05" {:status "active", :task nil}]
;                      ["2020/10/31 12:06" {:status "active", :task "WELKIN-76"}]
;                      ["2020/10/31 12:07" {:status "inactive", :task "WELKIN-76"}]
;                      ["2020/10/31 12:08" {:status "inactive", :task "WELKIN-76"}]
;                      ["2020/10/31 12:09" {:status "inactive", :task "WELKIN-76"}]
;                      ["2020/10/31 12:10" {:status "inactive", :task "WELKIN-76"}]
;                      ["2020/10/31 12:11" {:status "active", :task "WELKIN-76"}]
;                      ["2020/10/31 12:12" {:status "active", :task "WELKIN-76"}]
;                      ["2020/10/31 12:13" {:status "active", :task "WELKIN-76"}]
;                      ["2020/10/31 12:14" {:status "active", :task "WELKIN-76"}]
;                      ["2020/10/31 12:15" {:status "inactive", :task "WELKIN-76"}]
;                      ["2020/10/31 12:16" {:status "inactive", :task "WELKIN-76"}]
;                      ["2020/10/31 12:17" {:status "inactive", :task "WELKIN-76"}]
;                      ["2020/10/31 12:18" {:status "inactive", :task "WELKIN-76"}]
;                      ["2020/10/31 12:19" {:status "inactive", :task "WELKIN-76"}]
;                      ["2020/10/31 12:20" {:status "active", :task "WELKIN-76"}]
;                      ["2020/10/31 12:21" {:status "inactive", :task "WELKIN-76"}]
;                      ["2020/10/31 12:22" {:status "inactive", :task "WELKIN-76"}]
;                      ["2020/10/31 12:23" {:status "inactive", :task "WELKIN-76"}]
;                      ["2020/10/31 12:24" {:status "inactive", :task "WELKIN-76"}]
;                      ["2020/10/31 12:25" {:status "active", :task "WELKIN-76"}]
;                      ["2020/10/31 12:26" {:status "active", :task "WELKIN-76"}]
;                      ["2020/10/31 12:27" {:status "active", :task "WELKIN-76"}]
;                      ["2020/10/31 12:28" {:status "active", :task "WELKIN-76"}]
;                      ["2020/10/31 12:29" {:status "active", :task "WELKIN-76"}]
;                      ["2020/10/31 12:30" {:status "inactive", :task "WELKIN-76"}]
;                      ["2020/10/31 12:31" {:status "inactive", :task "WELKIN-76"}]
;                      ["2020/10/31 12:32" {:status "inactive", :task "WELKIN-76"}]
;                      ["2020/10/31 12:33" {:status "inactive", :task "WELKIN-76"}]
;                      ["2020/10/31 12:34" {:status "inactive", :task "WELKIN-76"}]
;                      ["2020/10/31 12:35" {:status "inactive", :task "WELKIN-76"}]
;                      ["2020/10/31 12:36" {:status "inactive", :task "WELKIN-76"}]
;                      ["2020/10/31 12:37" {:status "inactive", :task "WELKIN-76"}]
;                      ["2020/10/31 12:38" {:status "inactive", :task "WELKIN-76"}]
;                      ["2020/10/31 12:39" {:status "inactive", :task "WELKIN-76"}]]))
;


;; (->> {"2020/10/31 12:34" {:status "active", :task "WELKIN-76"}
;;       "2020/10/31 12:35" {:status "active", :task "WELKIN-76"}
;;       "2020/10/31 12:36" {:status "active", :task "WELKIN-76"}
;;       "2020/10/31 12:37" {:status "inactive", :task "WELKIN-76"}
;;       "2020/10/31 12:38" {:status "inactive", :task "WELKIN-76"}}
;;      (collect-package)
;;      (set-last-to-inactive)

;;      (map inactive->active)
;;      ;; (map set-inactive-log)
;;      ;; (reduce #(merge-packages %1 %2) [])
;;      ;; (map set-inactive-task)
;;      (map #(assoc %
;;                   :start (c/to-string (:start %))
;;                   :end (c/to-string (:end %)))))

;
;  (ls/local-set (.-webContents @w/main-window) "time" init))
