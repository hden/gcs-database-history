(ns io.debezium.relational.history.gcs
  (:require [clojure.string :as string]
            [cuid.core :refer [cuid]])
  (:import [java.nio.charset StandardCharsets]
           [com.google.cloud.storage Blob BlobId BlobInfo StorageOptions Storage$BlobListOption Storage$BlobWriteOption Blob$BlobSourceOption]
           [io.debezium.document DocumentReader DocumentWriter]
           [io.debezium.relational.history HistoryRecord])
  (:gen-class
    :name io.debezium.relational.history.gcs.GCSDatabaseHistory
    :extends io.debezium.relational.history.AbstractDatabaseHistory
    :init init
    :state state))

(def charset StandardCharsets/UTF_8)
(def reader (DocumentReader/defaultReader))
(def writer (DocumentWriter/defaultWriter))

(defprotocol AbstractBlob
  (get-content [this options]))

(extend Blob
  AbstractBlob
  {:get-content (fn [this options] (.getContent this options))})

(defn create-blob-id [bucket path]
  (BlobId/of bucket path))

(defn create-blob-info [bucket path]
  (let [id (create-blob-id bucket path)
        builder (BlobInfo/newBuilder id)]
    (doto builder
      (.setContentType "application/json"))
    (.build builder)))

(defn create-bucket-list-options [prefix]
  (into-array Storage$BlobListOption [(Storage$BlobListOption/currentDirectory) (Storage$BlobListOption/prefix prefix)]))

(defn create-blob-source-options []
  (into-array Blob$BlobSourceOption []))

(defn create-blob-write-options []
  (into-array Storage$BlobWriteOption []))

(defn create-history-record [x]
  (new HistoryRecord (.read reader x)))

(defn list-bucket [storage bucket prefix]
  (let [options (create-bucket-list-options prefix)
        page (.list storage bucket options)
        iterator (.iterator (.iterateAll page))]
    (iterator-seq iterator)))

(defn read-blob [blob]
  (let [options (create-blob-source-options)]
    (slurp (get-content blob options))))

(defn read-records [storage bucket prefix]
  (let [blobs (list-bucket storage bucket prefix)
        tx (comp (map read-blob)
                 (map #(create-history-record %)))]
    (into [] tx blobs)))

(defn write-record! [storage bucket prefix record]
  (let [path (format "%s%s.json" prefix (cuid))
        info (create-blob-info bucket path)
        options (create-blob-write-options)
        content (.write writer (.document record))
        buffer (java.nio.ByteBuffer/wrap (.getBytes content charset))]
    (with-open [writer (.writer storage info options)]
      (.write writer buffer))
    path))

(defn -init []
  [[] (atom {})])

(defn -configure [this config _]
  (let [state (.state this)
        bucket (.getString config "database.history.gcs.bucket")
        prefix (.getString config "database.history.gcs.prefix")
        storage (.getService (StorageOptions/getDefaultInstance))]
    (swap! state merge {:bucket bucket :prefix prefix :storage storage})))

(defn -exists [this]
  (let [{:keys [bucket prefix storage]} @(.state this)]
    (not (empty? (list-bucket storage bucket prefix)))))

(defn -storeRecord [this record]
  (when record
    (let [{:keys [bucket prefix storage]} @(.state this)]
      (write-record! storage bucket prefix record))))

(defn -recoverRecords [this consumer]
  (let [{:keys [bucket prefix storage]} @(.state this)]
    (doseq [record (read-records storage bucket prefix)]
      (.accept consumer record))))
