(defproject nml "1.1.0"
  :aot [nml.core]
  :dependencies [[com.github.clj-easy/graal-build-time "0.1.4"]
                 [instaparse "1.4.12"]
                 [org.clojure/clojure "1.11.1"]
                 [org.clojure/data.json "2.4.0"]
                 [org.clojure/tools.cli "1.0.214"]]
  :description "A query/modify utility for Fortran namelists"
  :license {:name "Apache License Version 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :main nml.core
  :target-path "target/%s"
  :uberjar-name "nml.jar"
  :url "https://github.com/maddenp/nml")
