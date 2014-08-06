(ns nml.core
  (:require [clojure.string    :as string])
  (:require [instaparse.core   :as insta ])
  (:gen-class))

(declare nk nkv nmlget nmlname nmlset nmlstr)

(def debug false)

(def parse (insta/parser (clojure.java.io/resource "grammar")))

(defn exe [tree commands]
  (if (empty? commands)
    tree
    (let [cmd (first  commands)
          arg (second commands)
          rst (drop 2 commands)]
      (case cmd
        "--get" (let [[nml key    ] (nk  arg)] (exe (nmlget tree nml key)     rst))
        "--set" (let [[nml key val] (nkv arg)] (exe (nmlset tree nml key val) rst))))))

(defn fail [& msg]
  (if msg (println (apply str msg)))
  (System/exit 1))

(defn nk [x]
  (string/split x #":" 2))

(defn nkv [x]
  (let [[nmlkey val] (string/split x #"=" 2)]
    (concat (nk nmlkey) (list val))))
  
(defn nmlget [tree nml key]
  (let [stmt     (last (filter #(= (nmlname %) nml) (rest tree)))
        nvsubseq (last (filter #(= (nmlname %) key) (rest (last stmt))))
        values   (last nvsubseq)
        value    (if (nil? values) "" (nmlstr values))]
    (println (str nml ":" key "=" value))
    tree))

(defn nmlname [x]
  (nmlstr (second x)))

(defn nmlset [tree nml key val & sub]
  (let [child (if sub :nvsubseq :stmt)
        match (if sub key nml)
        vnew  (if sub (fn [tree] (parse val :start :values)) #(nmlset % nml key val true))
        f     (fn [k v] [child k (if (= (nmlstr k) match) (vnew v) v)])]
    (insta/transform {child f} tree)))

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

(defn nmltree [fname]
  (try (parse (slurp fname))
       (catch Exception e (fail "Could not open namelist file '" fname "'"))))

(defn -main [& args]
  (alter-var-root #'*read-eval* (constantly false))
  (let [commands (butlast args)
        filename (last args)
        tree (nmltree filename)]
    (println (nmlstr (exe tree commands)))))
