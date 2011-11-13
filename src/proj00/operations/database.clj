(ns proj00.operations.database
  (:require [clojure.contrib.sql :as sql :only()])
  )

(def db {
  :classname "org.sqlite.JDBC"
  :subprotocol "sqlite"		; Protocol to use
  :subname "test.sqlite3"	; Location of the db
 })

(defmacro get-sql-metadata [db method & args] 
  `(sql/with-connection 
    ~db 
    (doall 
      (resultset-seq 
        (~method 
          (.getMetaData (sql/connection)) 
          ~@args)))))

(defn data-to-db [table column-names rows]
  (sql/with-connection db
    (sql/transaction
      (apply sql/create-table table (for [col column-names] [col]))
      (apply sql/insert-records table rows))))

(def tables-list 
 (get-sql-metadata db .getTables nil nil nil (into-array ["TABLE" "VIEW"])))

(defn cols-list [table]
  (let [data (get-sql-metadata db .getColumns nil nil table nil)]
  (map :column_name data)))

(defn db-to-data [id]
  (sql/with-connection db
    (sql/with-query-results dataframe [(format "select * from %s" id)]
      (doall dataframe))))
