(ns leiningen.repljs
  (:use
    [leiningen.compile :only (eval-in-project)]))

(defn start-browser-repl [project browser out-dir out-file port]
  (eval-in-project
    project
  `(let [start-repl#
         (fn []
           (cljs.repl/repl
             (cljs.repl.browser/repl-env :port (Integer/parseInt ~port))))

         create-html-file#
         (fn []
           (let [tmp-html# (java.io.File. "repl.html")]
             (spit
               tmp-html#
               (str "<html><head><meta charset='UTF-8'></head>"
                    "<body><script src='" ~out-dir "/goog/base.js'></script>"
                    "<script src='" ~out-file "'></script>"
                    "<script>goog.require('leiningen.repljs.browser');</script></body></html>"
                    ))
             tmp-html#))

         create-phantomjs-script#
         (fn [tmp-html#]
           (let [tmp-js# (java.io.File. (str ~out-dir "/phantom.js"))]
             (spit
               tmp-js#
               (str "new WebPage().open('"
                    (.getAbsolutePath tmp-html#)
                    "', function() {});"))
             tmp-js#))

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
               [~browser
                (.getAbsolutePath
                  (if (some #{"phantom" "phantomjs"} #{~browser})
                    (create-phantomjs-script# tmp-html#)
                    tmp-html#))])))
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

(defn repljs
  ([project]
   (start-rhino-repl project))
  ([project browser & [port & args]]
   (let [out-dir (.getAbsolutePath (java.io.File. (:cljs-output-dir project)))
         out-file (.getAbsolutePath (java.io.File. (str out-dir "/repl.js")))
         port (or port "9000")]
     (start-browser-repl project browser out-dir out-file port))))
