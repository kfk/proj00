(ns proj00.backend.io
  (:require [clojure.contrib.io :as io]
            [clojure.contrib.string :as st])
  (:use [incanter.core]
        [incanter.io :as iio]
        ))

(defn file-to-data [rs]
  (iio/read-dataset "tmp/tmp" 
    :delim (first (take (count (:delim rs)) (:delim rs))) 
    :header (contains? rs :csv-header)))

(defn wread-dataset [file]
  (iio/read-dataset file))

(defn tmp-file-copy [file] 
  (io/copy (io/file (:tempfile file)) (io/file "tmp/tmp")))

(defn from-tcms [sel-row]
  (with-open [input (clojure.java.io/reader "cms/tb_cms.txt")]
    (first
     (for [[_ number line] (map #(re-find #"@@(\d)+\s+(.*)" %)
                                (line-seq input))
           :when (= number (str sel-row))]
       line))))
