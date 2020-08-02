(ns app.renderer.forms.login
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            ["@material-ui/lab" :refer [Alert]]
            ["@material-ui/core" :refer [TextField
                                         Container
                                         Paper
                                         Snackbar
                                         Button
                                         Typography
                                         DialogTitle
                                         DialogContent
                                         DialogActions]]))

(def form-storage (atom {:login    nil
                         :password nil}))

(defn on-value-change [key value]
  (swap! form-storage #(conj % {key value})))

(rum/defc snackbar < rum/reactive [r]
  (let [{error :error} (rum/react (citrus/subscription r [:user]))]
    (rum/adapt-class Snackbar
                     {:open    error
                      :onClose #(citrus/dispatch! r :user :error-clean)}
                     (rum/adapt-class Alert
                                      {:onClose  #(citrus/dispatch! r :user :error-clean)
                                       :severity "warning"}
                                      "JIRA login warning"))))

(rum/defc image []
  [:img {:src    "img/icon-big.png"
         :width  100
         :height 100}])

(def login
  (rum/adapt-class   TextField
                     {:variant   "outlined"
                      :margin    "normal"
                      :required  true
                      :fullWidth true
                      :key       "login"
                      :id        "login"
                      :label     "Login"
                      :name      "login"
                      :onChange  #(let [val (.. % -target -value)]
                                    (on-value-change :login val))}))

(def pass
  (rum/adapt-class    TextField
                      {:variant   "outlined"
                       :margin    "normal"
                       :required  true
                       :fullWidth true
                       :key       "pass"
                       :id        "password"
                       :label     "Password"
                       :name      "password"
                       :type      "password"
                       :autoFocus true
                       :onChange  #(let [val (.. % -target -value)]
                                     (on-value-change :password val))}))

(def title
  (rum/adapt-class     Typography
                       {:component "h1"
                        :key       "title"
                        :variant   "h5"}
                       "TaskTracker"))

(def submit
  (rum/adapt-class   Button
                     {:className "signin-button"
                      :variant   "contained"
                      :key       "submit"
                      :type      "submit"
                      :color     "primary"
                      :fullWidth true}
                     "Sign in"))

(defn container [child]
  (rum/adapt-class       Container
                         {:key       "container"
                          :component "main"
                          :maxWidth  "xs"}
                         child))

(rum/defc form < rum/reactive
  [r child]
  [:form {:key      "form"
          :onSubmit #(do (.preventDefault %)
                         (citrus/dispatch! r :user :login @form-storage r))}
   child])

(defn paper [child]
  (rum/adapt-class     Paper
                       {:elevation 3
                        :key       "paper"
                        :className "login-paper"}
                       child))

(rum/defc div [child]
  [:div
   {:class "login-page"
    :key   "page-div"}
   child])

(rum/defc Login < rum/reactive
  [r]
  (container
    (form r (paper
              (rum/with-key (div
                              [(rum/with-key (image) "image")
                               (rum/with-key (snackbar r) "snackbar")
                               title
                               login
                               pass
                               submit]) "div")))))
