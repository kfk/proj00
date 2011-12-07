(ns proj00.backend.html-handlers)

(defn html-table-js [gattrs val_cols]
  [:script (format  
    "$(document).ready(function () {
	$.smtf('#_table', [%s]);
      });"
  (apply str (interpose "," (concat (repeat (count gattrs) "'text'") (repeat (count val_cols) "'number'")))))])

(defn html-table [dataset]
  [:table {:class "gridtable" :id "_table"}  
    [:thead [:tr (map (fn [x] [:th  (name x)]) (keys (first dataset)))]]
   (for [xs dataset] [:tr (map (fn [x] [:td x]) xs)])
   ])


