(ns nml.core
  (:gen-class)
  (:require [instaparse.core :as insta ])
  (:require [clojure.string  :as string]))

(declare nmlname nmlstr)

(def debug false)

(def parse (insta/parser (clojure.java.io/resource "grammar")))

(defn nmlget [tree nml key]
  (let [stmt      (last (filter #(= (nmlname %) nml) (rest tree)))
        nvsubseq  (last (filter #(= (nmlname %) key) (rest (last stmt))))
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

(defn nmlset [tree nml key val & sub]
  (let [child (if sub :nvsubseq :stmt)
        match (if sub key nml)
        vnew  (if sub (fn [tree] (parse val :start :values)) #(nmlset % nml key val true))
        f     (fn [k v] [child k (if (= (nmlstr k) match) (vnew v) v)])]
    (insta/transform {child f} tree)))

(defn -main [& args]
  (alter-var-root #'*read-eval* (constantly false))
  (let [filename (last args)
        tree (parse (slurp filename))]
    (println (nmlget (nmlset tree "n1" "s" "'orly'") "n1" "s"))))
