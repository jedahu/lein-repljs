(ns leiningen.repljs
  (:use
    [leiningen.compile :only (eval-in-project)]
    [leiningen.trampoline :only (*trampoline?*)]))

(defn start-browser-repl [project browser out-dir out-file port]
  (eval-in-project
    project
  `(let [start-repl#
         (fn []
           (cljs.repl/repl
             (cljs.repl.browser/repl-env :port (Integer/parseInt ~port)
                                         :working-dir ~out-dir)))

         create-html-file#
         (fn []
           (let [tmp-html# (java.io.File. "repljs.html")]
             (spit
               tmp-html#
               (str "<?xml version='1.0' encoding='UTF-8'?>"
                    "<html><head><meta charset='UTF-8'/></head>"
                    "<body><script src='" ~out-dir "/goog/base.js'></script>"
                    "<script src='" ~out-file "'></script>"
                    "<script>goog.require('leiningen.repljs.browser');</script></body></html>"
                    ))
             tmp-html#))

         build-browser-js#
         (fn []
           (cljs.closure/build
             '[(~'ns leiningen.repljs.browser
                 (:use [clojure.browser.repl :only (~'connect)]))
               (~'connect (str "http://localhost:" ~port "/repl"))]
             {:optimizations nil
              :pretty-print true
              :output-dir ~out-dir
              :output-to ~out-file}))

         start-browser#
         (fn [tmp-html#]
           (.exec
             (Runtime/getRuntime)
             (into-array
               [~browser (.getAbsolutePath tmp-html#)])))
         ]
     (build-browser-js#)
     (future
       (Thread/sleep 1000)
       (start-browser# (create-html-file#)))
     (start-repl#))
    nil nil
    '(require 'cljs.repl
              'cljs.repl.browser
              'cljs.closure)))

(defn start-rhino-repl [proj]
  (eval-in-project
    proj
    '(cljs.repl/repl (cljs.repl.rhino/repl-env))
    nil nil
    '(require 'cljs.repl 'cljs.repl.rhino)))

(defn repljs*
  ([project]
   (start-rhino-repl project))
  ([project browser & [port & args]]
   (let [out-dir (.getAbsolutePath (java.io.File. (get-in project [:cljs :output-dir] "public")))
         out-file (.getAbsolutePath (java.io.File. (str out-dir "/repljs.js")))
         port (or port "9000")]
     (start-browser-repl project browser out-dir out-file port))))

(defn repljs
  "Run a clojurescript repl with rhino or in the browser.

lein trampoline repljs                 => rhino repl
lein trampoline repljs browser [port]  => browser repl

The browser repl creates repljs.html in the project directory, and client.js
and repljs.js in the directory named by the :output-dir key of the :cljs
property."
  [project & args]
  (if *trampoline?*
    (apply repljs* project args)
    (do
      (println
        (str "The repljs plugin must be run by the trampoline:\n"
             "(lein trampoline repljs). This should be fixed in leiningen 2."))
      1)))
