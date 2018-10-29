(defproject gcs-database-history "0.1.0-SNAPSHOT"
  :description "A [debezium](https://github.com/debezium/debezium) DatabaseHistory implementation that stores the schema history in a GCS bucket."
  :url "https://github.com/hden/gcs-database-history"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [com.google.cloud/google-cloud-storage "1.50.0"]
                 [io.debezium/debezium-core "0.9.0.Alpha2"]
                 [cuid "0.1.1"]]
  :aot [io.debezium.relational.history.gcs]
  :test-paths ["test"]
  :profiles
  {:test
   {:dependencies [[org.slf4j/slf4j-nop "1.8.0-beta2"]]}})
