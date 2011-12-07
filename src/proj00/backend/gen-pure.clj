;Generic pure functions.
(ns proj00.backend.gen-pure
  (:require 
    [proj00.backend.database :as db]
    [clojure.contrib.string :as st]
    [clojure.contrib.math :as math]
    ))

(defn update-values [m f & args]
 (reduce (fn [r [k v]] (assoc r k (apply f v args))) {} m))

(defn sel-opt [dataset-nm]
  (let [cols (filter #(= (:datasetnm %) dataset-nm) (db/db-to-data "datasets"))]
   (map keyword (clojure.string/split (:selopt (first  cols)) #","))))

(defn sel-cols [dataset-nm]
  (let [cols (filter #(= (:datasetnm %) dataset-nm) (db/db-to-data "datasets"))]
   (map keyword (clojure.string/split (:selcols (first  cols)) #","))))

(defn format-numbers [coll sel-numb]
 (let [cols (map keyword (map #(st/trim (str " sum(" (name %) ") ")) sel-numb))] 
  (for [m coll] 
    (merge m (update-values (select-keys m cols) #(math/round(/ % 1000)))))))



