(ns proj00.views.welcome
  (:require [proj00.views.common :as common]
            [noir.content.pages :as pages])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpage "/welcome" []
         (common/layout       
           [:div#header
           [:h1 "Welcome to my-site"]
           [:ul [:li [:a {:href "/test"} "test"]]]
           [:p "Hello"]]
           [:p "Welcome to proj00"]))
