(ns proj00.views.common
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpartial layout [& content]
            (html5
              [:head
               [:title "proj00"]
               (include-css "/css/noir.css")
               (include-js "/js/jquery-1.4.4.min.js"
                           "/js/jquery.smtf.js"
                           "/js/jquery.tablesorter.js"
                           "/js/jquery.tablesorter.pager.js")]
              [:body
              [:div#wrapper
              [:div#header
                [:h1 "Happy data"] 
                [:ul 
                  [:li  
                   [:a {:href "/"} "Home"]
                   [:a {:href "/data/import"} "Data Import"]
                   [:a {:href "/dataset/create"} "Dataset Create"]
                   [:a {:href "/dataset/list"} "Datasets List"]]]]
                  content]]))
