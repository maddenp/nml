(defproject nml "0.6"
  :aot [nml.core]
  :dependencies [[instaparse "1.4.5"] [org.clojure/clojure "1.8.0"] [org.clojure/tools.cli "0.3.5"]]
  :description "A query/modify utility for Fortran namelists"
  :license {:name "Apache License Version 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :main nml.core
  :uberjar-name "nml.jar"
  :url "https://github.com/maddenp/nml")
