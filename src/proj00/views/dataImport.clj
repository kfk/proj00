(ns proj00.views.dataImport
  (:require [proj00.views.common :as common]
            [proj00.backend.database :as db]
            [proj00.backend.io :as io]
            [noir.validation :as vali]
            [noir.response :as resp]
            [noir.session :as ses])
  (:use noir.core
        hiccup.form-helpers
        hiccup.page-helpers 
        ))

(defpartial data-import-form [{:keys [u_file]}]
  [:p "Table name" (text-field "table-name")]
  [:p "Delimiter" (text-field "delim")]
  (file-upload :u_file)[:br]
  (check-box "csv-header") "Select if the file has a header" [:br]
  (submit-button "Add Dataset"))[:br]

(defpage "/data/import" {:as u_file}
  (common/layout 
    [:p "Please, choose a csv file to add to the dataset."]
    (form-to {:enctype "multipart/form-data"}
      [:post "/data/import"]    
      (data-import-form u_file))))

(defpage [:post "/data/import"] {:keys [u_file] :as rs} 
  (io/tmp-file-copy u_file)
  (let [dataset (io/file-to-data rs)] 
    (common/layout
      (db/data-to-db (:table-name rs) (:column-names dataset) (:rows dataset)) 
      [:p "done"])))

(defpage [:post "/data/import/accept"] {:as rs}
  (if (= (:accept-action rs) "true")
    (let [dataset (io/file-to-data rs)]
      (db/data-to-db (:table-name rs) (:column-names dataset) (:rows dataset))
      (common/layout
        [:p "Imported Successfully"]))
  (render "/data/import")))


