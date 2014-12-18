(ns onyx.plugin.core-async-test
  (:require [clojure.core.async :refer [>!! <!! chan close!]]
            [com.stuartsierra.component :as component]
            [onyx.peer.task-lifecycle-extensions :as l-ext]
            [onyx.plugin.core-async]
            [midje.sweet :refer :all]
            [onyx.system :refer [onyx-development-env]]
            [onyx.api]))

(def id (java.util.UUID/randomUUID))

(def env-config
  {:hornetq/mode :vm
   :hornetq/server? true
   :hornetq.server/type :vm
   :zookeeper/address "127.0.0.1:2185"
   :zookeeper/server? true
   :zookeeper.server/port 2185
   :onyx/id id})

(def peer-config
  {:hornetq/mode :vm
   :zookeeper/address "127.0.0.1:2185"
   :onyx/id id
   :onyx.peer/inbox-capacity 100
   :onyx.peer/outbox-capacity 100
   :onyx.peer/job-scheduler :onyx.job-scheduler/round-robin})

(def dev (onyx-development-env env-config))

(def env (component/start dev))

(def batch-size 25)

(def workflow
  [[:in :increment]
   [:increment :out]])

(def catalog
  [{:onyx/name :in
    :onyx/ident :core.async/read-from-chan
    :onyx/type :input
    :onyx/medium :core.async
    :onyx/consumption :concurrent
    :onyx/batch-size batch-size
    :onyx/batch-timeout 200
    :onyx/max-peers 1
    :onyx/doc "Reads segments from a core.async channel"}

   {:onyx/name :increment
    :onyx/fn :onyx.plugin.core-async-test/increment
    :onyx/type :function
    :onyx/consumption :concurrent
    :onyx/batch-size batch-size
    :onyx/batch-timeout 200}

   {:onyx/name :out
    :onyx/ident :core.async/write-to-chan
    :onyx/type :output
    :onyx/medium :core.async
    :onyx/consumption :concurrent
    :onyx/batch-size batch-size
    :onyx/batch-timeout 200
    :onyx/max-peers 1
    :onyx/doc "Writes segments to a core.async channel"}])

(defn increment [segment]
  (assoc segment :n (inc (:n segment))))

(def in-chan (chan 10000))

(def out-chan (chan 10000))

(defmethod l-ext/inject-lifecycle-resources :in
  [_ _] {:core-async/in-chan in-chan})

(defmethod l-ext/inject-lifecycle-resources :out
  [_ _] {:core-async/out-chan out-chan})

(def n-segments 100)

(doseq [n (range n-segments)]
  (>!! in-chan {:n n}))

(>!! in-chan :done)

(close! in-chan)

(def v-peers (onyx.api/start-peers! 1 peer-config))

(onyx.api/submit-job peer-config
                     {:catalog catalog
                      :workflow workflow
                      :task-scheduler :onyx.task-scheduler/round-robin})

(def results (doall (map (fn [_] (<!! out-chan)) (range (inc n-segments)))))

(let [expected (set (map (fn [x] {:n (inc x)}) (range n-segments)))]
  (fact (set (butlast results)) => expected)
  (fact (last results) => :done))

(doseq [v-peer v-peers]
  ((:shutdown-fn v-peer)))

(component/stop env)

