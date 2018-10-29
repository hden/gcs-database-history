(ns io.debezium.relational.history.gcs-test
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [io.debezium.relational.history.gcs :as gcs])
  (:import [io.debezium.config Configuration]
           [io.debezium.relational.history HistoryRecordComparator]))

(def ^:const bucket "gcs-database-history")
(def ^:const prefix "relational/history-")
(def ^:const fixture "relational/history-cjnu3yxr000003h5r04qlqq9s.json")

(deftest helpers
  (testing "create-blob-id"
    (let [path "bar/baz.txt"
          id (gcs/create-blob-id bucket path)]
      (are [x y] (= x y)
        (.getBucket id) bucket
        (.getName id) path)))

  (testing "create-blob-info"
    (let [info (gcs/create-blob-info bucket "bar/baz.json")]
      (is (= (.getContentType info) "application/json"))))

  (testing "list-bucket"
    (let [buckets (gcs/list-bucket bucket prefix)]
      (is (not (empty? buckets)))))

  (testing "create-history-record"
    (is (gcs/create-history-record "{}")))

  (testing "read-blob"
    (let [id (gcs/create-blob-id bucket fixture)
          blob (.get gcs/storage id)
          content (gcs/read-blob blob)]
      (is (not (empty? content)))))

  (testing "read-records"
    (is (not (empty? (gcs/read-records bucket prefix)))))

  (testing "write-record!"
    (let [record (gcs/create-history-record "{}")
          path (gcs/write-record! bucket prefix record)
          id (gcs/create-blob-id bucket path)]
      (is (string/includes? path prefix))
      (.delete gcs/storage id))))

(defn create-history [coll]
  (let [instance (new io.debezium.relational.history.gcs.GCSDatabaseHistory)
        config (Configuration/from coll)]
    (.configure instance config HistoryRecordComparator/INSTANCE)
    instance))

(defn create-consumer [state]
  (reify
    java.util.function.Consumer
    (accept [_ x]
      (swap! state conj x))))

(deftest GCSDatabaseHistory
  (testing ".configure"
    (let [instance (create-history {"database.history.gcs.bucket" bucket
                                    "database.history.gcs.prefix" prefix})
          state @(.state instance)]
      (are [x y] (= (get state x) y)
        :bucket bucket
        :prefix prefix)))

  (testing ".exists"
    (let [instance (create-history {"database.history.gcs.bucket" bucket
                                    "database.history.gcs.prefix" prefix})]
      (is (.exists instance))))

  (testing ".storeRecord"
    (let [instance (create-history {"database.history.gcs.bucket" bucket
                                    "database.history.gcs.prefix" prefix})]
      (is (not (.storeRecord instance nil)))))

  (testing ".recoverRecords"
    (let [instance (create-history {"database.history.gcs.bucket" bucket
                                    "database.history.gcs.prefix" prefix})
          records (atom [])]
      (.recoverRecords instance (create-consumer records))
      (is (not (empty? @records))))))
