(ns proj00.views.mainviews
  (:require [proj00.views.common :as common]
            [proj00.backend.database :as db]
            [noir.validation :as vali]
            [noir.response :as resp]
            [noir.session :as ses]
            [clojure.contrib.io :as io]
            [clojure.contrib.string :as st]
            [clojure.contrib.math :as math])
  (:use noir.core
        hiccup.form-helpers
        hiccup.page-helpers 
        [incanter core io]
        ))

;Calc functions
(defn update-values [m f & args]
 (reduce (fn [r [k v]] (assoc r k (apply f v args))) {} m))

(defn sel-opt [dataset-nm]
  (let [cols (filter #(= (:datasetnm %) dataset-nm) (db/db-to-data "datasets"))]
   (map keyword (clojure.string/split (:selopt (first  cols)) #","))))

(defn sel-cols [dataset-nm]
  (let [cols (filter #(= (:datasetnm %) dataset-nm) (db/db-to-data "datasets"))]
   (map keyword (clojure.string/split (:selcols (first  cols)) #","))))

(defn group-query [dataset-nm table-nm where gattrs]
  (str "select "
    (str (apply str (interpose ", " gattrs)) ",")
    (apply str (interpose ", " (map #(str " sum(" (name %) ")") (sel-cols dataset-nm))))
    (format " from %s " table-nm)
    (if (= where "") (str "") (str " where " where))
    (apply str " group by " (interpose ", " gattrs))))

(defn html-table-js [gattrs val_cols]
  [:script (format  
    "$(document).ready(function () {
	$.smtf('#_table', [%s]);
      });"
  (apply str (interpose "," (concat (repeat (count gattrs) "'text'") (repeat (count val_cols) "'number'")))))])

(defn html-table [dataset]
  [:table {:class "gridtable" :id "_table"}  
    [:thead [:tr (map (fn [x] [:th  (name x)]) (keys (first dataset)))]]
   (for [xs dataset] [:tr (map (fn [x] [:td x]) xs)])
   [:tfoot [:tr (map (fn [x] [:th (name x)]) (keys (first dataset)))]]
  ])

(defn sum-by [data attrs sel-cols]
  (let [aggregated (group-by (apply juxt attrs) data)]
    (for [k (keys aggregated)]
      (merge 
        (apply merge-with + (for [x (get aggregated k)] (select-keys x sel-cols))) 
        (zipmap attrs k)))))

(defn dataframe [table]
  (db/db-to-data table))

(def gattrs '("fy" "month" "pc0" "pc1"))

;Forms
(defpartial action-accept []
  (check-box "accept-action")"Do you want to procede importing data?"[:br]
  (submit-button "Accept"))

(defpage "/" []
  (common/layout
    [:p "Please, import some data (only csv import supported for now) and start your journey."]))

(defpage "/test" []
  (common/layout
    (let [data (read-dataset "/home/alessio/tmp/csv_test/transpose.csv" :header true)]
      (let [rows (:rows data)]
        (let [grouped (group-by (apply juxt [:month]) rows)]
           (apply str (interpose "\n" (for [k (distinct (map :pc rows))] 
             (str "|" k "|" (clojure.string/join "|" (for [n (range 1 13)]
               (get (first (filter #(= (:pc %) k) (get grouped [n]))) :sale)))))))))))) 

;/DATASET
(def tables-n (map :table_name db/tables-list))

(defpage "/dataset/list" []
    (common/layout
      (let [datasets (db/db-to-data "datasets")]
      (for [dataset (map :datasetnm datasets)]  
        [:ul [:li [:a {:href (str "show/"dataset"/"
                              (:tablenm (first (filter #(= (:datasetnm %) dataset) datasets))))} 
                   dataset]]]))))

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
          (drop-downs (sel-opt dataset-nm) ""))
        (html-table (vector (first (dataframe table-nm))))))

(defn format-numbers [coll sel-numb]
 (let [cols (map keyword (map #(st/trim (str " sum(" (name %) ") ")) sel-numb))] 
  (for [m coll] 
    (merge m (update-values (select-keys m cols) #(math/round(/ % 1000)))))))

(defpage [:post "/dataset/show/:dataset-nm/:table-nm"] {:keys [dataset-nm table-nm] :as rs}    
  (ses/put! :dgroups (remove #(= nil %) (list (:dgroups rs) (ses/get :dgroups))))
    (common/layout
      (form-to [:post (format "/dataset/show/%s/%s" dataset-nm table-nm)]
        (drop-downs (sel-opt dataset-nm) (distinct (flatten (ses/get :dgroups)))))
        (html-table-js (distinct (flatten (ses/get :dgroups))) (sel-cols dataset-nm))
      (let [l_gattrs (distinct (flatten (ses/get :dgroups)))]
        (html-table-js l_gattrs (sel-cols dataset-nm))
        (html-table 
          (format-numbers (db/query-to-data (group-query dataset-nm table-nm (:inp-where rs) l_gattrs)) (sel-cols dataset-nm))))
    (if (:clear rs) (ses/remove! :dgroups))))

