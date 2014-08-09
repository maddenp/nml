(ns nml.core
  (:require [clojure.string    :as string])
  (:require [instaparse.core   :as insta ])
  (:require [clojure.tools.cli :as cli   ])
  (:gen-class))

(declare nml-name nml-str nml-uniq)

; defs

(def debug false)

(def parse (insta/parser (clojure.java.io/resource "grammar")))

(def version "0.1")

;; defns

(defn fail [& msg]
  (doseq [line msg]
    (println line))
  (System/exit 1))

(defn nml-add [tree parent child match proxy]
  (let [missing  (fn [children] (not-any? #(= (nml-name %) match) children))
        vnew    #(parse proxy :start child)
        f        (fn [& children] (if (missing children)
                                    (into [parent (vnew)] children)
                                    (into [parent       ] children)))]
    (if (nil? tree) [parent (vnew)] (insta/transform {parent f} tree))))

(defn nml-get [tree nml key]
  (let [stmt     (last (filter #(= (nml-name %) (string/lower-case nml)) (rest tree)))
        nvsubseq (last (filter #(= (nml-name %) (string/lower-case key)) (rest (last stmt))))
        values   (last nvsubseq)]
    (if (nil? values) "" (nml-str values))))

(defn nml-gets [tree gets no-prefix]
  (doseq [[nml key] gets]
    (let [val (nml-get tree nml key)]
      (println (if no-prefix val (str nml ":" key "=" val))))))

(defn nml-name [x]
  (nml-str (second x)))

(defn nml-set [tree nml key val & sub]
  (let [child  (if sub :nvsubseq :stmt)
        match  (if sub (string/lower-case key) (string/lower-case nml))
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
  (let [ioerr  (str "Could not open namelist file '" fname "'.")
        result (try (parse (slurp fname)) (catch Exception e (fail ioerr)))
        child  :nvseq
        f      (fn [& v] (into [child] (nml-uniq v)))]
    (if (insta/failure? result)
      (let [{t :text l :line c :column} result]
        (fail (str "Error parsing '" fname "' at line " l " column " c ":")
              t
              (str (apply str (repeat (- c 1) " ")) "^")))
      (insta/transform {child f} result))))

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
   ["-h" "--help"      "Show usage information"                                                              ]
   ["-n" "--no-prefix" "Report values without 'namelist:key=' prefix"                                        ]
   ["-s" "--set n:k=v" "Set value of key 'k' in namelist 'n' to 'v'"  :assoc-fn assoc-set :parse-fn parse-set]
   ["-v" "--version"   "Show version information"                                                            ]])
  
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
    (if (:help options) (usage summary))
    (if (:version options) (do (println version) (System/exit 0)))
    (if (and gets sets) (fail "Do not mix get and set operations."))
    (let [file (first arguments)
          tree (if file (nml-tree file) [:s])]
      (if (and gets (not file)) (fail "Input file required for get operations."))
      (if debug (println tree))
      (cond gets  (nml-gets tree gets (:no-prefix options))
            sets  (println (string/trim (nml-str (nml-sets tree sets))))
            :else (println (string/trim (nml-str tree)))))))
