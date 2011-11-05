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
                content]]))
