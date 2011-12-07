(ns proj00.views.datasetCreate
  (:require [proj00.views.common :as common]
            [proj00.backend.database :as db]
            [proj00.backend.io :as iio]
            [noir.validation :as vali]
            [noir.response :as resp]
            [noir.session :as ses]
            [clojure.contrib.io :as io])
  (:use noir.core
        hiccup.form-helpers
        hiccup.page-helpers 
        [incanter core io]
        ))

(def tables-n (map :table_name db/tables-list))

(defn get-cols-nms [table] 
  (do (db/cols-list table)))

(defpartial form-dataset [cols-list table]
  "Dataset name: "(text-field "dataset-nm" "Input here dataset name")[:br]
  "Data source: "(if (= table nil)
    (assoc-in (drop-down "table" tables-n) [1 :onchange] "this.form.submit()")
    (assoc-in (drop-down "table" (conj (remove #(= % table) tables-n) table))
 [1 :onchange] "this.form.submit()"))[:br]  
  [:input {:type "submit" :value "Submit" :name "name"}][:br]
  (mapcat #(vector (check-box %) % [:br]) cols-list))

(defpartial dataset-create []
  [:p [:div#textnote (iio/from-tcms \2)]])

(defpage "/dataset/create" []
  (common/layout
    (dataset-create)
    (form-to [:post "/dataset/create"]
      (form-dataset(get-cols-nms (first tables-n)) nil))))

;TODO ADD Validation and check unique
(defpage [:post "/dataset/create"] {:as ks}
  (common/layout
    (dataset-create)
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


