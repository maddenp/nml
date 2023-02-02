(ns build
  (:require [clojure.java.io :as io]
            [clojure.tools.build.api :as b]))

(def libname "nml")
(def version "1.1.0")

(defn path [& xs] (str (apply io/file xs)))

(def basis (b/create-basis {:project "deps.edn"}))
(def target "target")
(def classes (path target "classes"))

(def grammar "grammar")

(defn clean [_]
  (b/delete {:path target}))

(defn uberjar [_]
  (clean nil)
  (b/copy-dir
    {:src-dirs ["src"]
     :target-dir target})
  (b/compile-clj
    {:basis basis
     :src-dirs ["src"]
     :class-dir classes})
  (b/copy-file
    {:src (path target grammar)
     :target (path classes grammar)})
  (b/uber
    {:basis basis
     :class-dir classes
     :main (symbol (str libname ".core"))
     :uber-file (path target (str libname ".jar"))}))
