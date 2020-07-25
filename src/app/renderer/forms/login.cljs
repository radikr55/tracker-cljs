(ns app.renderer.forms.login
  (:require [rum.core :as rum]
            [app.renderer.forms.components :as comp]
            [citrus.core :as citrus]))


;; (rum/defc form [child] [:form {:onSubmit #(do (.preventDefault %) (print (.. % -taget -value)))} child])
(rum/defc image []
  [:img {:src "img/icon-big.png" :width 100 :height 100 }])

(def email (comp/text-field
             {:variant      "outlined"
              :margin       "normal"
              :required     true
              :fullWidth    true
              :key          "email"
              :type         "email"
              :id           "email"
              :label        "Email Address"
              :name         "email"
              :autoComplete "email"
              :autoFocus    true}))

(def pass (comp/text-field
            {:variant   "outlined"
             :margin    "normal"
             :required  true
             :fullWidth true
             :key       "pass"
             :id        "password"
             :label     "Password"
             :name      "password"
             :type      "password"
             :autoFocus true}))

(defn submit1 [r]
  (comp/button
    {:className "signin-button"
     :variant   "contained"
     :key       "submit1"
     :color     "primary"
     :onClick   #(citrus/dispatch! r :loading :off)
     :fullWidth true}
    "Sign in1"))

(def submit (comp/button
              {:className "signin-button"
               :variant   "contained"
               :key       "submit"
               :type      "submit"
               :color     "primary"
               :fullWidth true}
              "Sign in"))

(def title (comp/typography {:component "h1"
                             :key       "title"
                             :variant   "h5"}
                            "TaskTracker"))

(rum/defc Login < rum/reactive
  [r]
  (comp/container
    {:component "main" :maxWidth "xs"}
    (comp/form {:onSubmit #(do (.preventDefault %)
                               (citrus/dispatch! r :loading :on))}
               (comp/div {:class "login-page"}
                         [( image )
                          title
                          email
                          pass
                          submit
                          ( submit1 r )]))))
