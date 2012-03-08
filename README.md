# Lein ReplJs

Run a clojurescript repl with rhino or in a browser.

    lein trampoline repljs                 => rhino repl
    lein trampoline repljs browser [port]  => browser repl

The browser repl creates `repljs.html` in the project directory, and
`client.js` and `repljs.js` in the directory named by the `:cljs {:output-dir ...}`
property (it defaults to `public`). If the browser command is `phantom` or `phantomjs` an additional file
`repljs-phantom.js` is created in the same directory.

Use `(load-namespace example.namespace)` instead of `require`, which will not
work. Alternatively, use a `ns` declaration to load namespaces:

    (ns temp.ns
      (:require [example.namespace :as ex]))

Due to Leiningen 1.x limitations the repljs plugin must be run via trampoline.
