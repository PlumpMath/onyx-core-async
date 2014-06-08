(ns onyx.plugin.core-async
  (:require [clojure.core.async :refer [>!! <!!]]
            [onyx.peer.task-lifecycle-extensions :as l-ext]))

(defmethod l-ext/read-batch [:input :core.async]
  [{:keys [onyx.core/task-map core-async/in-chan]}]
  (let [batch-size (:onyx/batch-size task-map)
        batch (filter identity (map (fn [_] (<!! in-chan)) (range batch-size)))]
    {:onyx.core/batch (doall batch)}))

(defmethod l-ext/decompress-batch [:input :core.async]
  [{:keys [onyx.core/batch]}]
  {:onyx.core/decompressed batch})

(defmethod l-ext/apply-fn [:input :core.async]
  [{:keys [onyx.core/decompressed]}]
  {:onyx.core/results decompressed})

(defmethod l-ext/ack-batch [:input :core.async]
  [{:keys [onyx.core/batch]}]
  {:onyx.core/acked (count batch)})

(defmethod l-ext/apply-fn [:output :core.async]
  [{:keys [onyx.core/decompressed]}]
  {:onyx.core/results decompressed})

(defmethod l-ext/compress-batch [:output :core.async]
  [{:keys [onyx.core/results]}]
  {:onyx.core/compressed results})

(defmethod l-ext/write-batch [:output :core.async]
  [{:keys [onyx.core/compressed core-async/out-chan]}]
  (doseq [segment compressed]
    (>!! out-chan segment)))

(defmethod l-ext/seal-resource [:output :core.async]
  [{:keys [core-async/out-chan]}]
  (>!! out-chan :done)
  {})

