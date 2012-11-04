(ns gene-network.core
  (:require [incanter.stats :as stats]
   			[clojure.string :as str]
            [clojure.java.io :as io]))

(def iqs [106 86 100 101 99 103 97 113 112 110])

(def tv-hours [7 0 27 50 28 29 20 12 6 17])

(stats/spearmans-rho iqs tv-hours) ;; -0.17575757575757578

(defn file-lines [file-name]
  (str/split-lines (slurp file-name)))

(def gene-list (file-lines "data/ecoli_genes.txt"))

(def tf-list (file-lines "data/ecoli_tfs.txt"))

(defn str-to-floats [string]
  (map #(Float/parseFloat %) (str/split string #"\s+")))

(defn get-expr-data []
  (let [v (transient [])]
    (with-open [rdr (io/reader "data/ecoli_expression.txt")]
      (doseq [line (line-seq rdr)]
        (conj! v (str-to-floats line))))
    (persistent! v)))

(def expr-data (get-expr-data))

(defn map-indices [ks v]
  (into {}
        (for [k ks]
          {k (.indexOf v k)})))

(def tf-indices (map-indices tf-list gene-list))

(defn tf-spearman-map []
  (for [[tf tfi] tf-indices]
    (for [genei (range (count gene-list))]
      (if (not= tfi genei)
        {tf [(gene-list genei) (stats/spearmans-rho (expr-data tfi)
                                                    (expr-data genei))]}))))

(tf-spearman-map)

(defn -main
  "I don't do a whole lot."
  [& args]
  (println "Hello, World!"))