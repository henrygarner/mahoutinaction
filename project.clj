(defproject mahoutinaction "1.0.0-SNAPSHOT"
  :description "Mahout in Action book examples"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.apache.mahout/mahout-core "0.7"]
                 [org.apache.mahout/mahout-math "0.7"]
                 [org.apache.mahout/mahout-utils "0.5"]]
  :java-options { :debug "true" }
  :jvm-opts ["-Xmx768m" "-d64" "-server" ;; "-XX:+NewRatio=12"
              "-XX:+UseParallelGC" "-XX:+UseParallelOldGC"]
  :main mahoutinaction.core)
