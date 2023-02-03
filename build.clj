(ns build
  (:require [clojure.java.io :as io]
            [clojure.tools.build.api :as b]))

(defn path [& xs] (str (apply io/file xs)))

(def basis (b/create-basis {:project "deps.edn"}))
(def grammar "grammar")
(def libname "nml")
(def target "target")
(def version "version")

(def classes (path target "classes"))

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
  (b/copy-file
    {:src (path target version)
     :target (path classes version)})
  (b/uber
    {:basis basis
     :class-dir classes
     :main (symbol (str libname ".core"))
     :uber-file (path target (str libname ".jar"))}))
