(ns mahoutinaction.ch07
  (:import [java.util ArrayList List]
           [org.apache.mahout.math Vector RandomAccessSparseVector VectorWritable DenseVector NamedVector]
           [org.apache.hadoop.conf Configuration]
           [org.apache.hadoop.fs FileSystem Path]
           [org.apache.hadoop.io SequenceFile$Writer SequenceFile$Reader LongWritable IntWritable Text]
           [org.apache.mahout.common.distance EuclideanDistanceMeasure]
           [org.apache.mahout.clustering.iterator DistanceMeasureCluster]
           [org.apache.mahout.clustering.kmeans KMeansDriver]
           [org.apache.mahout.clustering.classify WeightedVectorWritable]))

(def points
  [[1 1]
   [2 1]
   [1 2]
   [2 2]
   [3 3]
   [8 8]
   [9 8]
   [8 9]
   [9 9]])

(defn as-arraylist [points]
  (let [array (ArrayList.)]
    (dorun
     (map
      #(.add array (.assign (RandomAccessSparseVector. (count %))
                            (double-array %)))
      points))
    array))

(defn write-data [apples filepath]
  (let [conf (Configuration.)
        fs  (FileSystem/get conf)
        path (Path. filepath)
        writer (SequenceFile$Writer. fs conf path
                                     LongWritable
                                     VectorWritable)
        vec (VectorWritable.)]
    (dotimes [idx (count apples)]
      (let [v (.get apples idx)]
        (.set vec v)
        (.append writer (LongWritable. idx) vec)))
    (.close writer)))

(defn write-clusters [points filepath]
  (let [conf (Configuration.)
        fs (FileSystem/get conf)
        path (Path. filepath)
        writer (SequenceFile$Writer. fs conf path
                                     Text
                                     DistanceMeasureCluster)]

    (doall
     (map-indexed #(let [cluster (DistanceMeasureCluster. %2 %1 (EuclideanDistanceMeasure.))]
                     (.append writer (Text. (.getIdentifier cluster)) cluster)
                     ) points))
    (.close writer)))


; (write-data (get-points points) "resources/test-out.dat")


(defn run-kmeans [points clusters output]
  (let [conf (Configuration.)]
    (KMeansDriver/run (Path. points)
                      (Path. clusters)
                      (Path. output)
                      (EuclideanDistanceMeasure.)
                      0.001
                      10
                      true
                      0
                      false)))

(defn read-kmeans [filepath]
  (let [conf (Configuration.)
        fs (FileSystem/get conf)
        path (Path. filepath)
        reader (SequenceFile$Reader. fs path conf)
        key (IntWritable.)
        value (WeightedVectorWritable.)]
    (loop []
        (when (.next reader key value)
          (print (.toString value) " belongs to cluster " (.toString key))
          (recur)))
    (.close reader)))

(defn run []
  (write-data (as-arraylist points) "resources/points")
  (write-clusters (as-arraylist (take 2 points)) "resources/centroids/part-00000")
  (run-kmeans "resources/points" "resources/centroids" "resources/kmeans-out")
  (read-kmeans (str "resources/" DistanceMeasureCluster/CLUSTERED_POINTS_DIR "/part-m-00000"))
  )
