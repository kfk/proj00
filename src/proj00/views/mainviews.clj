(ns proj00.views.mainviews
  (:require [proj00.views.common :as common]
            [proj00.backend.database :as db]
            [noir.validation :as vali]
            [noir.response :as resp]
            [noir.session :as ses]
            [clojure.contrib.io :as io])
  (:use noir.core
        hiccup.form-helpers
        hiccup.page-helpers 
        [incanter core io]
        ))

;Calc functions

(defn sel-opt [dataset-nm]
  (let [cols (filter #(= (:datasetnm %) dataset-nm) (db/db-to-data "datasets"))]
   (map keyword (clojure.string/split (:selopt (first  cols)) #","))))

(defn sel-cols [dataset-nm]
  (let [cols (filter #(= (:datasetnm %) dataset-nm) (db/db-to-data "datasets"))]
   (map keyword (clojure.string/split (:selcols (first  cols)) #","))))

(defn group-query [dataset-nm table-nm gattrs]
  (str "select "
    (str (apply str (interpose ", " gattrs)) ",")
    (apply str (interpose ", " (map #(str " sum(" (name %) ")") (sel-cols dataset-nm))))
    (format " from %s " table-nm)
    (apply str " group by " (interpose ", " gattrs))))

(defn html-table [dataset]
  [:table {:class "gridtable"} 
   [:tr (map (fn [x] [:th (name x)]) (keys (first dataset)))]
   (for [xs dataset] [:tr (map (fn [x] [:td x]) xs)])
  ])

(defn sum-by [data attrs sel-cols]
  (let [aggregated (group-by (apply juxt attrs) data)]
    (for [k (keys aggregated)]
      (merge 
        (apply merge-with + (for [x (get aggregated k)] (select-keys x sel-cols))) 
        (zipmap attrs k)))))

;Generic forms
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
(defn dataframe [table]
  (db/db-to-data table))

(def tables-n (map :table_name db/tables-list))

(defpage "/dataset/list" []
    (common/layout
      (let [datasets (db/db-to-data "datasets")]
      ;(prn datasets)
      (prn (:tablenm (first (filter #(= (:datasetnm %) "Prova01") datasets))))
      (for [dataset (map :datasetnm datasets)]  
        [:ul [:li [:a {:href (str "show/"dataset"/"
                              (:tablenm (first (filter #(= (:datasetnm %) dataset) datasets))))} 
                   dataset]]]))))

;DATASET/CREATE
(defn get-cols-nms [table] 
  (do (db/cols-list table)))

(defpartial form-dataset [cols-list table]
  (text-field "dataset-nm" "Input here dataset name")[:br]
  (if (= table nil)
    (assoc-in (drop-down "table" tables-n) [1 :onchange] "this.form.submit()")
    (assoc-in (drop-down "table" (conj (remove #(= % table) tables-n) table))
 [1 :onchange] "this.form.submit()"))[:br]  
  [:input {:type "submit" :value "Submit" :name "name"}][:br]
  (mapcat #(vector (check-box %) % [:br]) cols-list) 
  )

(defpage "/dataset/create" []
  (common/layout
    (form-to [:post "/dataset/create"]
      (form-dataset(get-cols-nms (first tables-n)) nil))))

;TODO ADD Validation and check unique
(defpage [:post "/dataset/create"] {:as ks}
  (common/layout
    (let [table (ks :table)]
      (let [cols (get-cols-nms table)] 
        (form-to [:post "/dataset/create"] 
            (if (= (:name ks) nil)
            (form-dataset cols table)
            (let [sel-ks 
              (let [cl-ks (map name (keys ks))]
                (zipmap 
                    (vector "selcols" "selopt" "datasetnm" "tablenm") 
                    (vector 
                      (apply str (interpose "," (keys (into {} 
                        (filter #(= (second %) true) 
                          (merge-with = (zipmap cols cols)
                            (zipmap cl-ks cl-ks)))))))
                      (apply str (interpose "," (keys (into {} 
                        (apply dissoc (zipmap cols cols) (keys (zipmap cl-ks cl-ks)))))))
                      (:dataset-nm ks)
                      table)))]
                  (db/insert-records "datasets" sel-ks))))))))

;/DATASET/SHOW
;VARS: SelectColumns; DropDown-Opt
;TO DO: handle better the data. Revise "dataframe" function (should also take from other resources, not only db.
(defpartial drop-downs [nms]
  (assoc-in (drop-down "dgroups" nms) [1 :onclick] "this.form.submit()")[:br] 
  (submit-button "Refresh"))

(defpage "/dataset/show/:dataset-nm/:table-nm" {:keys [dataset-nm table-nm]}  
      (common/layout
        (form-to [:post (format "/dataset/show/%s/%s" dataset-nm table-nm)]
          (drop-downs (sel-opt dataset-nm)))
        (html-table (vector (first (dataframe table-nm))))))

(def gattrs '("year" "profitcenter" "month"))
(defpage [:post "/dataset/show/:dataset-nm/:table-nm"] {:keys [dataset-nm table-nm] :as rs}    
  (let [ _rs (assoc-in rs [:dgroups] (remove nil? (list (ses/get :dgroups) (:dgroups rs))))] 
    (ses/put! :dgroups (:dgroups rs))
      (common/layout
      (form-to [:post (format "/dataset/show/%s/%s" dataset-nm table-nm)]
        (drop-downs (sel-opt dataset-nm)))
      (html-table (db/query-to-data (group-query dataset-nm table-nm gattrs))))))




(defpage [:post "/dataset/show-/:dataset-nm/:table-nm"] {:keys [dataset-nm table-nm] :as rs}    
  (let [ _rs (assoc-in rs [:dgroups] (remove nil? (list (ses/get :dgroups) (:dgroups rs))))] 
    (ses/put! :dgroups (:dgroups rs))
      (common/layout
      (form-to [:post (format "/dataset/show/%s/%s" dataset-nm table-nm)]
        (drop-downs (sel-opt dataset-nm)))
      (html-table
        (sum-by 
          (dataframe table-nm) 
          (reverse (map keyword (remove (set [""]) (flatten (vals (dissoc _rs :dataset-nm :table-nm))))))
          (sel-cols dataset-nm)))
      [:a {:href (format "/dataset/show/%s/%s" dataset-nm table-nm)} "Back"]
    )))

