(ns nml.core
  (:gen-class)
  (:require [instaparse.core :as insta ])
  (:require [clojure.string  :as string]))

(declare prpr)

(def debug false)
(def parse (insta/parser (clojure.java.io/resource "grammar")))

(defn nmlget [tree namelist key]
  (let [stmts     (rest tree)
        stmt      (last (filter #(= (nmlname %) namelist) stmts))
        nvsubseqs (rest (last stmt))
        nvsubseq  (last (filter #(= (nmlname %) key) nvsubseqs))
        values    (last nvsubseq)]
    (if (nil? values) "" (prpr values))))

(defn nmlname [x]
  (prpr (second x)))

(defn prpr [x]
  (let [k (first x)
        v (rest  x)
        cjoin    #(string/join "," %)
        delegate #(map prpr %)
        ds       (fn [v] (delegate (sort-by #(nmlname %) v)))
        list2str #(apply str (map prpr %))
        sf       #(prpr (first %))
        sl       #(prpr (last %))]
    (if debug (println (str "k=" k " v=" v)))
    (apply str (case k
                 :s        (ds v)
                 :array    [(sf v) (sl v)]
                 :c        (sf v)
                 :colon    ":"
                 :comma    ","
                 :comment  ""
                 :complex  ["(" (cjoin (delegate v)) ")"]
                 :dataref  (delegate v)
                 :dec      (delegate v)
                 :dot      "."
                 :exp      [(first v) (sl v)]
                 :false    "f"
                 :int      (delegate v)
                 :junk     ""
                 :logical  (sf v)
                 :name     (map string/lower-case v)
                 :nvseq    (ds v)
                 :nvsubseq ["  " (sf v) "=" (sl v) "\n"]
                 :partref  (sf v)
                 :percent  "%"
                 :r        (sf v)
                 :real     (delegate v)
                 :sect     ["(" (list2str v) ")"]
                 :sep      v
                 :sign     v
                 :slash    v
                 :star     "*"
                 :stmt     ["&" (sf v) "\n" (list2str (rest v)) "/\n"]
                 :string   v
                 :true     "t"
                 :uint     v
                 :value    (delegate v)
                 :values   (cjoin (delegate v))
                 :ws       ""
                 :wsopt    ""))))

(defn -main [& args]
  (alter-var-root #'*read-eval* (constantly false))
  (let [namelist-file (last args)
        tree (parse (slurp namelist-file))]
    (println (nmlget tree "n1" "a"))))
;;   (println (prpr tree))))
