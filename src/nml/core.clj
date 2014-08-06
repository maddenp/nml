(ns nml.core
  (:require [clojure.string    :as string])
  (:require [instaparse.core   :as insta ])
  (:require [clojure.tools.cli :as cli   ])
  (:gen-class))

(declare nmlget nmlname nmlset nmlstr)

; defs

(def debug false)

(def parse (insta/parser (clojure.java.io/resource "grammar")))

;; defns

(defn fail [& msg]
  (if msg (println (apply str msg)))
  (System/exit 1))

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

(defn nmlxform [tree sets]
  (loop [t tree s sets]
    (if (empty? s)
      t
      (let [[nml key val] (first s)]
        (recur (nmlset t nml key val) (rest s))))))

(defn nmltree [fname]
  (try (parse (slurp fname))
       (catch Exception e (fail "Could not open namelist file '" fname "'."))))

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
  (let [[nmlkey val] (string/split x #"=" 2)
        [nml key] (parse-get nmlkey)]
    [nml key val]))

(def cliopts
  [["-g" "--get n:k"   "get value of key 'k' in namelist 'n'"        :assoc-fn assoc-get :parse-fn parse-get ]
   ["-s" "--set n:k=v" "set value of key 'k' in namelist 'n' to 'v'" :assoc-fn assoc-set :parse-fn parse-set]])
  
;; main

(defn -main [& args]
  (alter-var-root #'*read-eval* (constantly false))
  (let [{:keys [options arguments summary]} (cli/parse-opts args cliopts)
        gets (:get options)
        sets (:set options)
        tree (nmltree (first arguments))]
    (if (and gets sets) (fail "Do not mix get and set operations."))
    (cond gets (doseq [[nml key] gets] (nmlget tree nml key))
          sets (println (nmlstr (nmlxform tree sets)))
          :else (println (nmlstr tree)))))
