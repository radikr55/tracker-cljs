(ns app.renderer.controllers.project
  (:require [app.renderer.effects :as effects]))

(def initial-state {:left
                    [{:category "", :list [{:title "All Projects"}]}
                     {:category "Aristar",
                      :list
                      [{:title "Aristar - Video Analytics App",
                        :code  "AR_VAAPP",
                        :id    12015}]}
                     {:category "Chronosignum",
                      :list     [{:title "Chronosignum", :code "DTS", :id 12017}]}
                     {:category "DearHealth",
                      :list
                      [{:title "DEARHealth", :code "DEAR", :id 13802}
                       {:title "DearHealth - AuthoringTool", :code "DH_AT", :id 10210}
                       {:title "DearHealth - AuthoringTool v2", :code "DAT2", :id 11917}
                       {:title "DearHealth - mobile", :code "DM", :id 12701}
                       {:title "DearHealth - Patient Application (iOS)",
                        :code  "DH_PAI",
                        :id    10212}
                       {:title "DearHealth - PatientApp (Android)",
                        :code  "DH_PAA",
                        :id    10213}]}
                     {:category "Field Hockey",
                      :list
                      [{:title "Field Hockey - Video Analytics App",
                        :code  "FH_VAA",
                        :id    12807}]}
                     {:category "Hit Sezona",
                      :list
                      [{:title "Hit Sezona - Site of travel agency",
                        :code  "HIT_SITE",
                        :id    10601}]}
                     {:category "Invincible",
                      :list
                      [{:title "Invincible - Boats website", :code "INB_BW", :id 12009}]}
                     {:category "Kalorimeta",
                      :list     [{:title "eLive-Kalorimeta", :code "EL", :id 13000}]}
                     {:category "LateralLink",
                      :list
                      [{:title "Lateral Link / Mainspring general",
                        :code  "LLSUP",
                        :id    12400}
                       {:title "LateralLink.com", :code "LLINK", :id 11909}
                       {:title "Mainspring Legal home page - ex laterallink.com",
                        :code  "LLINKF",
                        :id    12002}]}
                     {:category "Lincoln",
                      :list
                      [{:title "CapTech (Lincoln)", :code "CPT", :id 12806}
                       {:title "HillSDK ex CapTech (Lincoln)", :code "HILL", :id 12900}
                       {:title "Lincoln", :code "LIN", :id 12611}]}
                     {:category "MapCase",
                      :list
                      [{:title "MapCase - Wayn", :code "MAP_WAYN", :id 11710}
                       {:title "MySmartContact T&M", :code "SC_TM", :id 12801}]}
                     {:category "MixFit",
                      :list     [{:title "MixFit", :code "MIX", :id 13505}]}
                     {:category "Pinxter",
                      :list
                      [{:title "LifeTrek", :code "LIF", :id 13400}
                       {:title "Pinxter - FlightApp", :code "FLIGHT", :id 13004}
                       {:title "Pinxter - My CMO Club", :code "PN_CCL", :id 12802}]}
                     {:category "PoorMountain",
                      :list     [{:title "PoorMountain", :code "PRM", :id 13804}]}
                     {:category "Ready-Set-Sync",
                      :list
                      [{:title "Ready Set Sync - TVtibi (Android)",
                        :code  "RSS_TB_AND",
                        :id    10700}
                       {:title "Ready Set Sync - TVtibi (iOS)",
                        :code  "RSS_TB_IOS",
                        :id    10214}
                       {:title "Ready Set Sync - TVtibi (WinPhone)",
                        :code  "RSS_TB_WP",
                        :id    10313}
                       {:title "Ready Set Sync - TVtibi - Stage 4",
                        :code  "RSS_TB_S4",
                        :id    10308}
                       {:title "Ready Set Sync - TVtibi - Stage 5",
                        :code  "RSS_TB_S5",
                        :id    10318}]}
                     {:category "ReadyTech",
                      :list
                      [{:title "ReadyTech - AdminPortal", :code "RT_AP", :id 11905}
                       {:title "ReadyTech - Axis", :code "RT_SS", :id 11602}
                       {:title "ReadyTech - Background Assistant",
                        :code  "RT_BA",
                        :id    11003}
                       {:title "ReadyTech - ClassManager", :code "RT_CM", :id 10204}
                       {:title "ReadyTech - Device Mapper", :code "RT_DM", :id 11402}
                       {:title "ReadyTech - HyperView", :code "RT_MM", :id 11908}
                       {:title "ReadyTech - ILP", :code "RT_ILP", :id 10102}
                       {:title "ReadyTech - LabTracker", :code "RT_LT", :id 10501}
                       {:title "ReadyTech - Portal Helper", :code "RT_PHELP", :id 12610}
                       {:title "ReadyTech - Public API (Express Gateway)",
                        :code  "RT_API",
                        :id    11101}
                       {:title "ReadyTech - Purchase portal", :code "RT_PURCH", :id 10900}
                       {:title "ReadyTech - Readytech.com", :code "RT_CSITE", :id 11902}
                       {:title "ReadyTech - SaaS Labs Assistant",
                        :code  "RTSAAS",
                        :id    11703}
                       {:title "ReadyTech - Sales Demo Portal", :code "RT_SDP", :id 11301}
                       {:title "ReadyTech - Self-Paced Portal", :code "RT_SPP", :id 10701}
                       {:title "ReadyTech - STEP", :code "RT_STEP", :id 10103}
                       {:title "ReadyTech - System Agent", :code "RT_SA", :id 13604}
                       {:title "ReadyTech - TMS", :code "RT_TMS", :id 10222}
                       {:title "ReadyTech - Viewer", :code "RT_VIEWER", :id 10201}]}
                     {:category "SYBE",
                      :list
                      [{:title "SYBE - CMS1450+Dental Claim Form",
                        :code  "SYBE_NCLMF",
                        :id    10307}
                       {:title "SYBE - Support", :code "SYBE_SPRT", :id 10306}]}
                     {:category "SmartContacts",
                      :list
                      [{:title "MapCase - Smart Contact", :code "MAP_SMART", :id 11709}
                       {:title "MSC - Middleware", :code "MSC_MID", :id 12607}
                       {:title "SmartContact - MySmartContact",
                        :code  "MYSMART",
                        :id    12600}
                       {:title "SmartContact - WebSite", :code "SC_WEB", :id 11920}]}
                     {:category "Softarex",
                      :list
                      [{:title "Finmatex v3", :code "FM", :id 13807}
                       {:title "Softarex - BaseballVideoAnalytics",
                        :code  "SA_BVA",
                        :id    12019}
                       {:title "Softarex - Business development",
                        :code  "SA_BDEVEL",
                        :id    10800}
                       {:title "Softarex - Company Site (2015)",
                        :code  "SA_COMSITE",
                        :id    10221}
                       {:title "Softarex - Competencies", :code "SA_CM", :id 13501}
                       {:title "Softarex - Contacts DB", :code "SA_CDB", :id 11711}
                       {:title "Softarex - Data analysis system",
                        :code  "SA_DAS",
                        :id    12504}
                       {:title "Softarex - DB Records Creator", :code "SA_DRC", :id 14105}
                       {:title "Softarex - Develop of internal documentation",
                        :code  "SA_DOC",
                        :id    11919}
                       {:title "Softarex - Face recognition tool",
                        :code  "SA_FCREC",
                        :id    11914}
                       {:title "Softarex - Financial system", :code "SA_FN", :id 13500}
                       {:title "SOFTAREX - FinMatex ChatBot",
                        :code  "SA_CHATBOT",
                        :id    11918}
                       {:title "Softgetarex - FINMATEX Marketing", :code "SA_FMM", :id 13203}
                       {:title "Softarex - FinmatexUI", :code "SFUI", :id 12700}
                       {:title "Softarex - GigZone", :code "SA_GIGZONE", :id 10209}
                       {:title "Softarex - GreenVaro", :code "SA_GVARO", :id 11702}
                       {:title "Softarex - IT Platforms", :code "SA_PL", :id 13506}
                       {:title "Softarex - Jira Mapper Test", :code "SA_JMT", :id 13003}
                       {:title "Softarex - LeadGen", :code "SA_LG", :id 13801}
                       {:title "Softarex - LUNAR Sales & Marketing",
                        :code  "SA_LNM",
                        :id    14101}
                       {:title "Softarex - MeetAppNow", :code "SA_MEETAPP", :id 11401}
                       {:title "Softarex - Meeting for company's managers",
                        :code  "SA_MEETMAN",
                        :id    12008}
                       {:title "Softarex - Meetings (2017)", :code "SA_MEET1", :id 12004}
                       {:title "Softarex - Merchant Middleware", :code "SA_MM", :id 12609}
                       {:title "Softarex - People Partner and Talent Management",
                        :code  "SA_PPTM",
                        :id    11916}
                       {:title "Softarex - PR CodeNforcer", :code "SA_PCODE", :id 11001}
                       {:title "Softarex - PR GigZone", :code "SA_PGIG", :id 11000}
                       {:title "Softarex - PR Softarex Site",
                        :code  "SA_PSASITE",
                        :id    11002}
                       {:title "Softarex - Secondary internal tasks (2017)",
                        :code  "SA_ISECT",
                        :id    12007}
                       {:title "Softarex - Self education of automation testing ",
                        :code  "SA_AT",
                        :id    13603}
                       {:title "Softarex - Servers_Management", :code "SOF", :id 11406}
                       {:title "Softarex - Softarex Marketing and PR",
                        :code  "SA_SMPR",
                        :id    11200}
                       {:title "Softarex - Softarex Recruiting ",
                        :code  "SA_RECR",
                        :id    12809}
                       {:title "Softarex - Softarex Sites (2017)",
                        :code  "SA_SASITES",
                        :id    11921}
                       {:title "Softarex - Softarex Sites (2018)",
                        :code  "SA_SITES",
                        :id    12803}
                       {:title "Softarex - TechCommHub Site",
                        :code  "SA_TCHSITE",
                        :id    11912}
                       {:title "Softarex - TraidingPlatform", :code "SA_TRP", :id 11911}
                       {:title "Softarex - WAITO Sales & Marketing",
                        :code  "SA_WTM",
                        :id    14000}
                       {:title "Softarex-Slack-Bot", :code "SSBOT", :id 12805}]}
                     {:category "naisA",
                      :list     [{:title "naisA - nfa", :code "NA_NFA", :id 11712}]}],
                    :right
                    [{:category "My Recent Tasks",
                      :list
                      [{:title
                        "WELKIN-91 - OC-1229: [L3] [Dev] [Risalto] Behavior of Assessment Responses endpoint",
                        :code "WELKIN-91",
                        :id   94570}
                       {:title
                        "WELKIN-76 - WDZ-548: Publish Notification events for SMS subscriptions",
                        :code "WELKIN-76",
                        :id   94391}
                       {:title
                        "WELKIN-69 - WDZ-335: Support SNS subscriptions from Workshop Integration Tools",
                        :code "WELKIN-69",
                        :id   94163}
                       {:title
                        "WELKIN-57 - WDZ-334: Publish Notification events from event hub to SNS topics",
                        :code "WELKIN-57",
                        :id   93740}
                       {:title
                        "WELKIN-9 - WDZ-272: Add ability to activate/deactivate assessments in workshop",
                        :code "WELKIN-9",
                        :id   92002}
                       {:title "SA_TT-33 - Deploy environment",
                        :code  "SA_TT-33",
                        :id    93077}
                       {:title "WELKIN-46 - WDZ-408: NotificationEvents for App Messages",
                        :code  "WELKIN-46",
                        :id    93125}
                       {:title
                        "WELKIN-31 - WDZ-354: Deprecated Assessment that was filled in the past does not open",
                        :code "WELKIN-31",
                        :id   92694}
                       {:title
                        "WELKIN-42 - WDZ-429: Assessment URL can still be referenced as a variable and be sent to patients",
                        :code "WELKIN-42",
                        :id   93046}
                       {:title
                        "WELKIN-36 - WDZ-360: [5]:Find Worker Calendar by WorkerId or Worker Email",
                        :code "WELKIN-36",
                        :id   92874}]}
                     {:category "My Open Tasks",
                      :list
                      [{:title "WELKIN-103 - test", :code "WELKIN-103", :id 95000}]}]})

(defmulti control (fn [event] event))

(defmethod control :init [_ [reconciler]]
  {:state initial-state})

(defmethod control :get [_ [reconciler] state]
  ;; (let [token (effects/local-storage
  ;;               reconciler :project
  ;;               {:method :get
  ;;                :key    :token})]
  ;;   {:http {:endpoint :project
  ;;           :params   token
  ;;           :method   :post
  ;;           :on-load  :success
  ;;           :on-error :error}})
  {:state state})

(defmethod control :success [event [args] state]
  ;; (let [body (js->clj
  ;;              (.parse js/JSON (:body args))
  ;;              :keywordize-keys true)]
  ;;   {:state         body
  ;;    :local-storage {:method :set
  ;;                    :key    :body
  ;;                    :data   body}})
  {:state state})

(defmethod control :error [_ [error] state]
  (print error))
