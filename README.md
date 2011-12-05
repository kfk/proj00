Janara
=====

Janara is a simple tool to group and filter medium sized datasets (those that are a trouble to hold in a spreadsheet).

The work flow is the following:
-----

1) First, a text based file (csv) must be imported.
2) Second, a dataset must be created. At the moment datasets are only used in order to define what data can be grouped. 
3) Datasets will be listed in "/datasets/list" and from there accessible to be queried.

Javascripts
-----

Filter is done client side. The reason for that is simplicity and avoiding to hit the server for every query. A lot of logic seems to be shifting on the client nowdaws and thus Janara has been for me also an exercise to learn a bit of javascript (which I find painful, by the way) and jQuery. 

Indeed, I wrote a small, sweet, wildly alpha (the plugin may come out of the monitor and try to kill you), jQuery plugin called smtf (super mega table filter) that to now seems to work well. Suggestions, critics, threats, all well accepted.

Testing
-----

I did not do tests. Mostly because I do not know how to do it yet and the application is so small that I believe it is better to focus on the main thing.

Note
-----

Janara at the moment is an exercise and it is extremely alpha. The hope is that it will provide some helpful insights for beginners on web programming and clojure. I am still deciding if implementing more features or writing Janara in another functional language (Haskell or OCaml) as my intial scope was to improve my understanding of functional programming.
