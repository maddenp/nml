(defproject nml "1.0.0"
  :aot [nml.core]
  :dependencies [[instaparse "1.4.9"]
                 [org.clojure/clojure "1.9.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.cli "0.3.7"]]
  :description "A query/modify utility for Fortran namelists"
  :license {:name "Apache License Version 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :main nml.core
  :target-path "target/%s"
  :uberjar-name "nml.jar"
  :url "https://github.com/maddenp/nml")
