(defproject
  lein-repljs "0.1.0"

  :description
  "clojurescript rhino and browser repl for leiningen"

  :dependencies
  [[org.clojure/clojure "1.3.0"]
   [org.clojure/clojurescript "0.0-927"]]

  :repositories
  {"sonatype-oss"
   "http://oss.sonatype.org/content/groups/public/"}

  :eval-in-leiningen true)
