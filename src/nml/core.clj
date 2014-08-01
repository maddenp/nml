(ns nml.core
  (:gen-class)
  (:require [instaparse.core :as insta ])
  (:require [clojure.string  :as string]))

(def debug false)

(def parse (insta/parser (clojure.java.io/resource "grammar")))

(defn prpr [x]
  (let [k (first x)
        v (rest  x)
        cjoin    #(string/join "," %)
        delegate #(map prpr %)
        ds       (fn [v] (delegate (sort-by #(prpr (second %)) v)))
        list2str #(apply str (map prpr %))
        sf       #(prpr (first %))
        sl       #(prpr (last %))]
    (if debug (println (str "k=" k " v=" v)))
    (apply str
           (case k
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
  (let [namelist-file (last args)]
    (println (prpr (parse (slurp namelist-file))))))
