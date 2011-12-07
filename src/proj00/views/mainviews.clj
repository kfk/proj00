(ns proj00.views.mainviews
  
  (:require [proj00.views.common :as common]
            [proj00.backend.database :as db]
            [proj00.backend.io :as iio]
            [proj00.backend.html-handlers :as html-h]
            [proj00.backend.gen-pure :as gen-pure]
            [noir.validation :as vali]
            [noir.response :as resp]
            [noir.session :as ses]
            [clojure.contrib.io :as io] 
            )
  (:use noir.core
        hiccup.form-helpers
        hiccup.page-helpers 
        [incanter core io]
        ))

;Calc functions
(defn dataframe [table]
  (db/db-to-data table))

;Forms
(defpartial action-accept []
  (check-box "accept-action")"Do you want to procede importing data?"[:br]
  (submit-button "Accept"))

(defpage "/" []
  (common/layout
    [:p (iio/from-tcms \1)]))

(defpage "/test" []
  (common/layout
    [:p (iio/from-tcms \1)]))
;/DATASET
(def tables-n (map :table_name db/tables-list))

(defpage "/dataset/list" []
    (common/layout
      (let [datasets (db/db-to-data "datasets")]
      (for [dataset (map :datasetnm datasets)]  
        [:ul 
         [:li 
          [:a {:href (str "show/"dataset"/" (:tablenm (first (filter #(= (:datasetnm %) dataset) datasets))))} dataset]]]))))

;/DATASET/SHOW
;VARS: SelectColumns; DropDown-Opt
;TO DO: handle better the data. Revise "dataframe" function (should also take from other resources, not only db.
(defpartial drop-downs [nms gattrs]
  (label "lb_GroupBy" "Group By") 
  (assoc-in (drop-down "dgroups" nms) [1 :onclick] "this.form.submit()")[:br]
  "Grouped by: " (interpose " , " gattrs)[:br] 
  [:label "Filter by"
  (text-area "inp-where")][:br]
  (submit-button "Refresh")
  (assoc-in (submit-button "Clear") [1 :name] "clear"))

(defpage "/dataset/show/:dataset-nm/:table-nm" {:keys [dataset-nm table-nm]}  
      (common/layout
        (form-to [:post (format "/dataset/show/%s/%s" dataset-nm table-nm)]
          (drop-downs (gen-pure/sel-opt dataset-nm) ""))
        (html-h/html-table (vector (first (dataframe table-nm))))))

(defpage [:post "/dataset/show/:dataset-nm/:table-nm"] {:keys [dataset-nm table-nm] :as rs}    
  (ses/put! :dgroups (remove #(= nil %) (list (:dgroups rs) (ses/get :dgroups))))
    (common/layout
      (form-to [:post (format "/dataset/show/%s/%s" dataset-nm table-nm)]
        (drop-downs (gen-pure/sel-opt dataset-nm) (distinct (flatten (ses/get :dgroups)))))
        (html-h/html-table-js (distinct (flatten (ses/get :dgroups))) (gen-pure/sel-cols dataset-nm))
      (let [l_gattrs (distinct (flatten (ses/get :dgroups)))]
        (html-h/html-table-js l_gattrs (gen-pure/sel-cols dataset-nm))
        (html-h/html-table 
          (gen-pure/format-numbers (db/query-to-data (db/group-query (gen-pure/sel-cols dataset-nm) table-nm (:inp-where rs) l_gattrs)) (gen-pure/sel-cols dataset-nm))))
    (if (:clear rs) (ses/remove! :dgroups))))

