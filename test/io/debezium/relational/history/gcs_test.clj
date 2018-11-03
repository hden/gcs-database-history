(ns io.debezium.relational.history.gcs-test
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [io.debezium.relational.history.gcs :as gcs])
  (:import [com.google.api.gax.paging Page]
           [com.google.cloud WriteChannel]
           [com.google.cloud.storage Storage]
           [io.debezium.config Configuration]
           [io.debezium.relational.history HistoryRecordComparator]))

(def ^:const bucket "bucket")
(def ^:const prefix "prefix")
(def history (.getBytes "{}"))

(defrecord Blob [content]
  gcs/AbstractBlob
  (get-content [_ options]
    content))

(defn create-blob [content]
  (new Blob content))

(defn create-page [contents]
  (reify Page
    (iterateAll [this]
      (map create-blob contents))))

(defn create-storage [{:keys [contents]}]
  (reify Storage
    (list [this bucket options]
      (create-page contents))
    (writer [this info options]
      (reify WriteChannel
        (isOpen [this]
          (println "isOpen"))
        (close [this]
          (println "close"))
        (write [this buffer]
          1)))))

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
    (let [storage (create-storage {:contents [history]})
          buckets (gcs/list-bucket storage bucket prefix)]
      (is (not (empty? buckets)))))

  (testing "create-history-record"
    (is (gcs/create-history-record "{}")))

  (testing "read-blob"
    (let [blob (create-blob history)]
      (is (= "{}" (gcs/read-blob blob)))))

  (testing "read-records"
    (let [storage (create-storage {:contents [history]})]
      (is (not (empty? (gcs/read-records storage bucket prefix))))))

  (testing "write-record!"
    (let [storage (create-storage {:contents [history]})
          record (gcs/create-history-record "{}")
          path (gcs/write-record! storage bucket prefix record)]
      (is (string/includes? path prefix)))))

(defn create-history [coll]
  (let [instance (new io.debezium.relational.history.gcs.GCSDatabaseHistory)
        storage (create-storage {:contents [history]})
        config (Configuration/from coll)]
    (.configure instance config HistoryRecordComparator/INSTANCE)
    (swap! (.state instance) assoc :storage storage)
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
