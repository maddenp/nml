(ns nml.core
  (:gen-class)
  (:require [instaparse.core :as insta ])
  (:require [clojure.string  :as string]))

(declare nmlname nmlstr nmlsub)

(def debug false)

(def parse (insta/parser (clojure.java.io/resource "grammar")))

(defn nmlfind [tree nml key]
  (let [stmts     (rest tree)
        stmt      (last (filter #(= (nmlname %) nml) stmts))
        nvsubseqs (rest (last stmt))]
    (last (filter #(= (nmlname %) key) nvsubseqs))))

(defn nmlget [tree nml key]
  (let [nvsubseq  (nmlfind tree nml key)
        values    (last nvsubseq)]
    (if (nil? values) "" (nmlstr values))))

(defn nmlname [x]
  (nmlstr (second x)))

(defn nmlstr [x]
  (let [k (first x)
        v (rest  x)
        cjoin    #(string/join "," %)
        delegate #(map nmlstr %)
        ds       (fn [v] (delegate (sort-by #(nmlname %) v)))
        list2str #(apply str (map nmlstr %))
        sf       #(nmlstr (first %))
        sl       #(nmlstr (last %))]
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

(defn nmlset [tree nml key val]
  (let [swap-if (fn [k v] (if (= (nmlstr k) nml) [k (nmlsub v nml key val)] [k v]))]
    (println (insta/transform {:stmt swap-if} tree))))

(defn nmlsub [nvseq nml key val]
  (let [val-tree (parse val :start :values)
        swap-if (fn [k v] (if (= (nmlstr k) key) [k val-tree] [k v]))]
    (insta/transform {:nvsubseq swap-if} nvseq)))

(defn -main [& args]
  (alter-var-root #'*read-eval* (constantly false))
  (let [filename (last args)
        tree (parse (slurp filename))]
    (nmlset tree "n1" "s" "'orly'")))
;;   (println (nmlget tree "n1" "s"))))
;;   (println (nmlstr tree))))
