(ns proj00.views.form
  (:require [proj00.views.common :as common]
            [proj00.operations.database :as db]
            [noir.validation :as vali]
            [noir.response :as resp]
            [clojure.contrib.io :as io])
  (:use noir.core
        hiccup.form-helpers
        hiccup.page-helpers 
        [incanter core io]
        ))

(defpartial user-fields [{:keys [u_file]}]
  (file-upload :u_file))  

(defpage "/" []
  (common/layout
    [:p "It works!"]))

(defpage "/dataset/add" {:as u_file}
  (common/layout 
    [:p "Please, choose a csv file to add to the dataset."]
    (form-to {:enctype "multipart/form-data"}
      [:post "/dataset/add"]
      [:p "Table name" (text-field "table-name")]
      (user-fields u_file)
      (submit-button "Add Dataset"))))

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
        (zipmap attrs k)
        ))))

;/DATASET
(defn dataframe [id]
  (db/db-to-data id))

(def tables-n (map :table_name db/tables-list))

(defpage [:post "/dataset/add"] {:keys [u_file] :as params}
  (io/copy (io/file (:tempfile u_file)) (io/file "tmp/tmp"))
  (let [dataset (read-dataset "tmp/tmp")] 
    (db/data-to-db (:table-name params) (:column-names dataset) (:rows dataset))
    (common/layout
      [:p "Imported Successfully"])))

(defpage "/dataset/list" []
    (common/layout
      (for [table-n tables-n] [:ul [:li [:a {:href (str "show/"table-n)} table-n]]])))

;DATASET/CREATE
(defn get-cols-nms [table] 
  (do (db/cols-list table)))

(defpartial form-dataset [cols-list]
  (text-field "dataset_nm" "Input here dataset name")[:br]
  (drop-down "table" tables-n)
  (submit-button "Refresh")[:br]
  (mapcat #(vector (check-box %) % [:br]) cols-list) 
  )

(defpage "/dataset/create" []
  (common/layout
    (form-to [:post "/dataset/create"]
      (form-dataset (get-cols-nms (first tables-n))))))

(defpage [:post "/dataset/create"] {:as ks}
  (common/layout
    (prn ks)
    (let [table (ks :table)]
      (form-to [:post "/dataset/create"] 
        (form-dataset (get-cols-nms table))))))


;/DATASET/SHOW
;VARS: SelectColumns; DropDown-Opt
;TO DO: handle better the data. Revise "dataframe" function (should also take from other resources, not only db.

(def sel-opt
  {"alessio" [" " :col0], "test" [" " :col0 :col1]})

(def sel-cols
  [:col2 :col3])

(defpartial drop-downs [nms]
  ;(for [nm (keys nms)] (drop-down nm (get nms nm)))
  (mapcat #(vector (drop-down % (nms %)) [:br]) (keys nms))
  (submit-button "Refresh"))

(defpage "/dataset/show/:id" {:keys [id]}
  (common/layout
    (form-to [:post (format "/dataset/show/%s" id)]
      (drop-downs sel-opt)
        )
    (html-table (dataframe id))))

(defpage [:post "/dataset/show/:id"] {:keys [id] :as t}
  (common/layout
    [:p (:test t)]
    (prn (map keyword (remove (set [""]) (flatten (vals (dissoc t :id))))))
    (html-table 
      (sum-by 
        (dataframe id) 
        (reverse (map keyword (remove (set [""]) (flatten (vals (dissoc t :id))))))
        sel-cols))
    [:a {:href (format "/dataset/show/%s" id)} "Back"]
    ))
