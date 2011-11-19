(ns proj00.views.common
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpartial layout [& content]
            (html5
              [:head
               [:title "proj00"]
               (include-css "/css/noir.css")]
              [:body
               [:div#wrapper
              [:div#header
                [:h1 "Welcome to my-site"] 
                [:ul 
                  [:li  
                   [:a {:href "/dataset/create"} "Dataset Create"]
                   [:a {:href "/dataset/list"} "Datasets List"]]]]
                  content]]))
