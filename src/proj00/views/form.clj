(ns proj00.views.form
  (:require [proj00.views.common :as common]
            [proj00.operations.database :as db]
            [noir.validation :as vali]
            [noir.response :as resp]
            [noir.session :as ses]
            [clojure.contrib.io :as io])
  (:use noir.core
        hiccup.form-helpers
        hiccup.page-helpers 
        [incanter core io]
        ))

(defpartial data-import-form [{:keys [u_file]}]
  (file-upload :u_file)[:br]
  (check-box "csv_header") "Select if the file has a header" [:br]
  (submit-button "Add Dataset"))[:br]

(defpage "/" []
  (common/layout
    [:p "Please, import some data (only csv import supported for now) and start your journey."]))

;DATA/IMPORT
(defpage "/data/import" {:as u_file}
  (common/layout 
    [:p "Please, choose a csv file to add to the dataset."]
    (form-to {:enctype "multipart/form-data"}
      [:post "/data/import"]
      [:p "Table name" (text-field "table-name")]
      (data-import-form u_file))))

(defpage [:post "/data/import"] {:keys [u_file] :as params}
  (io/copy (io/file (:tempfile u_file)) (io/file "tmp/tmp"))
  (let [dataset (read-dataset "tmp/tmp" :delim \tab)] (prn dataset)))
    ;(db/data-to-db (:table-name params) (:column-names dataset) (:rows dataset))
    ;(common/layout
    ;  [:p "Imported Successfully"])))

;Calc functions
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

;/DATASET
(defn dataframe [id]
  (db/db-to-data id))

(def tables-n (map :table_name db/tables-list))

(defpage "/dataset/list" []
    (common/layout
      (let [db-data (db/db-to-data "datasets")]
      (prn (map :tablenm db-data))
      (prn (filter #(= (:datasetnm %) "prova3") db-data))
      (for [table-n (map :datasetnm db-data)] [:ul [:li [:a {:href (str "show/"table-n"/prova1")} table-n]]]))))

(defpage "/dataset/list-bk" []
    (common/layout
      (for [table-n tables-n] [:ul [:li [:a {:href (str "show/"table-n)} table-n]]])))

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

(defn sel-opt [dataset-nm]
  (let [cols (filter #(= (:datasetnm %) dataset-nm) (db/db-to-data "datasets"))]
   (map keyword (clojure.string/split (:selopt (first  cols)) #","))))

(defn sel-cols [dataset-nm]
  (let [cols (filter #(= (:datasetnm %) dataset-nm) (db/db-to-data "datasets"))]
   (map keyword (clojure.string/split (:selcols (first  cols)) #","))))

(defpartial drop-downs [nms]
  (assoc-in (drop-down "dgroups" nms) [1 :onclick] "this.form.submit()")[:br] 
  (submit-button "Refresh"))

(defpage "/dataset/show/:dataset-nm/:table-nm" {:keys [dataset-nm table-nm]}  
      (common/layout
        (form-to [:post (format "/dataset/show/%s/%s" dataset-nm table-nm)]
          (drop-downs (sel-opt dataset-nm)))
        (html-table (dataframe table-nm))))

(defpage [:post "/dataset/show/:dataset-nm/:table-nm"] {:keys [dataset-nm table-nm] :as rs}    
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

