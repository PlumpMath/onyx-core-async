(ns onyx.plugin.core-async
  (:require [clojure.core.async :refer [>!! <!!]]
            [onyx.peer.pipeline-extensions :as p-ext]))

(defmethod p-ext/read-batch [:input :core.async]
  [{:keys [onyx.core/task-map core-async/in-chan]}]
  (let [batch-size (:onyx/batch-size task-map)
        batch (->> (range batch-size)
                   (map (fn [_] {:input :core.async
                                :message (<!! in-chan)}))
                   (filter identity))]
    {:onyx.core/batch (doall batch)}))

(defmethod p-ext/decompress-batch [:input :core.async]
  [{:keys [onyx.core/batch]}]
  {:onyx.core/decompressed (filter identity (map :message batch))})

(defmethod p-ext/strip-sentinel [:input :core.async]
  [{:keys [onyx.core/decompressed]}]
  {:onyx.core/tail-batch? (= (last decompressed) :done)
   :onyx.core/requeue? false
   :onyx.core/decompressed (remove (partial = :done) decompressed)})

(defmethod p-ext/apply-fn [:input :core.async]
  [{:keys [onyx.core/decompressed]}]
  {:onyx.core/results decompressed})

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

(defn take-segments!
  "Takes segments off the channel until :done is found.
   Returns a seq of segments, including :done."
  [ch]
  (loop [x []]
    (let [segment (<!! ch)]
      (let [stack (conj x segment)]
        (if-not (= segment :done)
          (recur stack)
          stack)))))

