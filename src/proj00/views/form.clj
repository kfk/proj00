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

(defpage "/dataset/add" {:as u_file}
  (common/layout 
    [:div#header
    [:h1 "Welcome to my-site"]
    [:ul [:li [:a {:href "/test"} "test"]]]]
    [:p "Please, choose a csv file to add to the dataset."]
    (form-to {:enctype "multipart/form-data"}
      [:post "/dataset/add"]
      (user-fields u_file)
      (submit-button "Add Dataset"))))

(defn html-table [dataset]
  [:table {:class "gridtable"} 
   [:tr (map (fn [x] [:th (name x)]) (keys (first dataset)))]
   (for [xs dataset] [:tr (map (fn [x] [:td x]) xs)])
  ])

(defn my-group-by [col sel-cols dataset]
  (let [datasetg (group-by col dataset)]
    (for [k (keys datasetg)] (assoc (apply merge-with + 
      (for [x (get datasetg k)] (select-keys x sel-cols))) :k k))))

(defpage [:post "/dataset/add"] {:keys [u_file]}
  (io/copy (io/file (:tempfile u_file)) (io/file "tmp/tmp"))
  (let [dataset (read-dataset "tmp/tmp")] 
    (db/data-to-db "test" (:column-names dataset) (:rows dataset))
    (common/layout (html-table dataset))))

(defpage "/dataset/list" []
    (common/layout
      (let [tables-n (map :table_name db/tables-list)]
        (for [table-n tables-n] [:ul [:li [:a {:href (str "table/"table-n)} table-n]]]))))

(defpage "/dataset/table/:id" {:keys [id]}
  (let [dataframe (db/db-to-data id)]
    (common/layout
      (form-to [:post (format "/dataset/table/%s" id)]
        (drop-down "test" ["1" "2"])
        (submit-button "Refresh"))
      (html-table (my-group-by :col0 [:col1 :col2] dataframe))
      )))

(defpage [:post "/dataset/table/:id"] {:keys [id] :as t}
  (common/layout
    [:p (:test t)]
    (prn t)))
