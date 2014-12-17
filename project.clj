(defproject com.mdrogalis/onyx-core-async "0.5.0-SNAPSHOT"
  :description "Onyx plugin for core.async"
  :url "https://github.com/MichaelDrogalis/onyx-core-async"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.mdrogalis/onyx "0.5.0-SNAPSHOT"]]
  :profiles {:dev {:dependencies [[midje "1.6.2"]
                                  [com.stuartsierra/component "0.2.1"]]
                   :plugins [[lein-midje "3.1.3"]]}})
