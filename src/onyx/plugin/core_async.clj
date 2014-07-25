(ns onyx.plugin.core-async
  (:require [clojure.core.async :refer [>!! <!!]]
            [onyx.peer.pipeline-extensions :as p-ext]))

(defmethod p-ext/read-batch [:input :core.async]
  [{:keys [onyx.core/task-map core-async/in-chan]}]
  (let [batch-size (:onyx/batch-size task-map)
        batch (filter identity (map (fn [_] (<!! in-chan)) (range batch-size)))]
    {:onyx.core/batch (doall batch)}))

(defmethod p-ext/decompress-batch [:input :core.async]
  [{:keys [onyx.core/batch]}]
  {:onyx.core/decompressed batch})

(defmethod p-ext/apply-fn [:input :core.async]
  [{:keys [onyx.core/decompressed]}]
  {:onyx.core/results decompressed})

(defmethod p-ext/ack-batch [:input :core.async]
  [{:keys [onyx.core/batch]}]
  {:onyx.core/acked (count batch)})

(defmethod p-ext/apply-fn [:output :core.async]
  [{:keys [onyx.core/decompressed]}]
  {:onyx.core/results decompressed})

(defmethod p-ext/compress-batch [:output :core.async]
  [{:keys [onyx.core/results]}]
  {:onyx.core/compressed results})

(defmethod p-ext/write-batch [:output :core.async]
  [{:keys [onyx.core/compressed core-async/out-chan]}]
  (doseq [segment compressed]
    (>!! out-chan segment))
  {})

(defmethod p-ext/seal-resource [:output :core.async]
  [{:keys [core-async/out-chan]}]
  (>!! out-chan :done)
  {})

