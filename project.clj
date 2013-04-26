(defproject clodav "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories [["ettrema-repo" "http://milton.io/maven"]]
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [io.milton/milton-api "2.4.2.7"]
                 [io.milton/milton-server-ce "2.4.2.7"]
                 [compojure "1.1.5"]
                 [ring "1.1.8"]
                 [org.slf4j/slf4j-log4j12 "1.6.6"]
                 [org.apache.commons/commons-io "1.3.2"]])
