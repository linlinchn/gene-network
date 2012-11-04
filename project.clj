(defproject gene-network "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://stellar.mit.edu/S/course/6/fa12/6.047/courseMaterial/homework/assignments/assignment4/assignment/1/ps3.2.pdf"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [incanter "1.3.0" :exclusions [org.mongodb/mongo-java-driver]]]
  :jvm-opts ["-Xmx1g"])
