(ns nml.core
  (:require [clojure.string    :as string])
  (:require [instaparse.core   :as insta ])
  (:require [clojure.tools.cli :as cli   ])
  (:gen-class))

(declare nml-name nml-str nml-uniq)

; defs

(def debug false)

(def parse (insta/parser (clojure.java.io/resource "grammar")))

;; defns

(defn fail [& msg]
  (if msg (println (apply str msg)))
  (System/exit 1))

(defn nml-add [tree parent child match proxy]
  (let [missing  (fn [children] (not-any? #(= (nml-name %) match) children))
        vnew    #(parse proxy :start child)
        f        (fn [& children] (if (missing children)
                                    (into [parent (vnew)] children)
                                    (into [parent       ] children)))]
    (if (nil? tree) [parent (vnew)] (insta/transform {parent f} tree))))

(defn nml-get [tree nml key values]
  (let [stmt     (last (filter #(= (nml-name %) nml) (rest tree)))
        nvsubseq (last (filter #(= (nml-name %) key) (rest (last stmt))))
        values   (last nvsubseq)]
    (if (nil? values) "" (nml-str values))))

(defn nml-gets [tree gets values]
  (doseq [[nml key] gets]
    (let [val (nml-get tree nml key values)]
      (println (if values val (str nml ":" key "=" val))))))

(defn nml-name [x]
  (nml-str (second x)))

(defn nml-set [tree nml key val & sub]
  (let [child  (if sub :nvsubseq :stmt)
        match  (if sub key nml)
        parent (if sub :nvseq :s)
        proxy  (if sub (str match "=0") (str "&" match " /"))
        vnew   (if sub (fn [tree] (parse val :start :values)) #(nml-set % nml key val true))
        f      (fn
                 ([k v] (if (= (nml-str k) match) [child k (vnew v  )] [child k v]))
                 ([k  ] (if (= (nml-str k) match) [child k (vnew nil)] [child k  ])))]
    (insta/transform {child f} (nml-add tree parent child match proxy))))

(defn nml-sets [tree sets]
  (loop [t tree s sets]
    (if (empty? s)
      t
      (let [[nml key val] (first s)]
        (recur (nml-set t nml key val) (rest s))))))

(defn nml-str [x]
  (let [k         (first x)
        v         (rest  x)
        cjoin    #(string/join "," %)
        delegate #(map nml-str %)
        ds        (fn [v] (delegate (sort-by #(nml-name %) v)))
        list2str #(apply str (map nml-str %))
        sf       #(nml-str (first %))
        sl       #(nml-str (last %))]
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

(defn nml-tree [fname]
  (let [errmsg (str "Could not open namelist file '" fname "'.")
        tree   (try (parse (slurp fname)) (catch Exception e (fail errmsg)))
        child  :nvseq
        f      (fn [& v] (into [child] (nml-uniq v)))]
    (insta/transform {child f} tree)))

(defn nml-uniq [values]
  (loop [head (first values) tail (rest values) tree []]
    (if (nil? head)
      tree
      (let [name #(nml-name (second %))
            copy  (some #(= (name head) (name %)) tail)]
        (recur (first tail) (rest tail) (into tree (if copy [] [head])))))))

;; cli

(defn assoc-get [m k v]
  (let [gets (:get m [])]
    (assoc m :get (into gets [v]))))

(defn assoc-set [m k v]
  (let [sets (:set m [])]
    (assoc m :set (into sets [v]))))

(defn parse-get [x]
  (string/split x #":" 2))

(defn parse-set [x]
  (let [[nml+key val] (string/split x #"=" 2)
        [nml key] (parse-get nml+key)]
    [nml key val]))

(def cliopts
  [["-g" "--get n:k"   "Get value of key 'k' in namelist 'n'"         :assoc-fn assoc-get :parse-fn parse-get]
   ["-s" "--set n:k=v" "Set value of key 'k' in namelist 'n' to 'v'"  :assoc-fn assoc-set :parse-fn parse-set]
   ["-v" "--values"    "Report values without 'namelist:key=' prefix"                                        ]
   ["-h" "--help"      "Show usage information"                                                              ]])
  
;; main

(defn usage [summary]
  (doseq [x [""
             "Usage: nml [options] file"
             ""
             "Options:"
             ""
             summary
             ""]]
    (println x))
  (System/exit 0))

(defn -main [& args]
  (alter-var-root #'*read-eval* (constantly false))
  (let [{:keys [options arguments summary]} (cli/parse-opts args cliopts)
        gets (:get options)
        sets (:set options)]
    (if (and gets sets) (fail "Do not mix get and set operations."))
    (if (:help options) (usage summary))
    (let [tree (nml-tree (first arguments))]
      (cond gets  (nml-gets tree gets (:values options))
            sets  (println (nml-str (nml-sets tree sets)))
            :else (println (nml-str tree))))))
