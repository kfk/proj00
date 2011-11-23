(ns proj00.backend.io
  (:require [clojure.contrib.io :as io])
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


