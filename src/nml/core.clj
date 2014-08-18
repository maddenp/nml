(ns nml.core
  (:require [clojure.string    :as string])
  (:require [instaparse.core   :as insta ])
  (:require [clojure.tools.cli :as cli   ])
  (:gen-class))

(declare nml-get nml-name nml-parse nml-set nml-str)

;; defs

(def msgs
  {
   :create+in     "-i/--in not valid with -c/--create."
   :get+out       "-o/--out may not be used with -g/--get."
   :get+set       "-g/--get and -s/--set may not be mixed."
   :multi-in      "-i/--in may be specified only once."
   :multi-out     "-o/--out may be specified only once."
   :set+no-prefix "-n/--no-prefix not valid with -s/--set."
   })

(def parse (insta/parser (clojure.java.io/resource "grammar")))

(def version "0.1")

;; utility defns

(defn- fail [& lines]
  (binding [*out* *err*]
    (doseq [line lines]
      (println (str "nml: " line)))
    (System/exit 1)))

(defn- usage [summary]
  (doseq [x [""
             "Usage: nml [options]"
             ""
             "Options:"
             ""
             summary
             ""]]
    (println x))
  (System/exit 0))

;; nml private defns

(defn- nml-add [tree parent child match proxy]
  (let [missing  (fn [children] (not-any? #(= (nml-name %) match) children))
        vnew    #(nml-parse proxy child)
        f        (fn [& children] (if (missing children)
                                    (into [parent (vnew)] children)
                                    (into [parent       ] children)))]
    (if (nil? tree) [parent (vnew)] (insta/transform {parent f} tree))))

(defn- nml-gets [tree gets no-prefix]
  (doseq [[nml key] gets]
    (let [val (nml-get tree nml key)]
      (println (if no-prefix val (str nml ":" key "=" val))))))

(defn- nml-map [tree]
  (let [f0 (fn [& nvsubseqs   ] (into {} nvsubseqs))
        f1 (fn [dataref values] { (nml-str dataref) (nml-str values) })
        f2 (fn [& stmts       ] (into {} stmts))
        f3 (fn [name nvseq    ] { (nml-str name) nvseq })]
    (insta/transform {:nvseq f0 :nvsubseq f1 :s f2 :stmt f3 } tree)))

(defn- nml-name [x]
  (nml-str (second x)))

(defn- nml-parse [s start]
  (let [result (parse s :start start)]
    (if (insta/failure? result)
      (let [{t :text l :line c :column} result]
        (fail (str "Parse error at line " l " column " c ":")
              t
              (str (apply str (repeat (- c 1) " ")) "^")))
      result)))
  
(defn- nml-sets [tree sets]
  (loop [t tree s sets]
    (if (empty? s)
      t
      (let [[nml key val] (first s)]
        (if (nil? val) (fail (str "No value supplied for key '" key "'.")))
        (recur (nml-set t nml key val) (rest s))))))

(defn- nml-str [x]
  (let [key       (first x)
        val       (rest  x)
        cjoin    #(string/join "," %)
        delegate #(map nml-str %)
        delesort  (fn [val] (delegate (sort-by #(nml-name %) val)))
        list2str #(apply str (map nml-str %))
        strfirst #(nml-str (first %))
        strlast  #(nml-str (last %))]
    (apply str (case key
                 :s        (string/join "\n" (delesort val))
                 :array    [(strfirst val) (strlast val)]
                 :c        (strfirst val)
                 :colon    ":"
                 :comma    ","
                 :comment  ""
                 :complex  ["(" (cjoin (delegate val)) ")"]
                 :dataref  (delegate val)
                 :dec      (delegate val)
                 :dot      "."
                 :exp      [(first val) (strlast val)]
                 :false    "f"
                 :int      (delegate val)
                 :junk     ""
                 :logical  (strfirst val)
                 :name     (map string/lower-case val)
                 :nvseq    (string/join " " (delesort val))
                 :nvsubseq [(strfirst val) "=" (strlast val)]
                 :partref  (strfirst val)
                 :percent  "%"
                 :r        (strfirst val)
                 :real     (delegate val)
                 :sect     ["(" (list2str val) ")"]
                 :sep      val
                 :sign     val
                 :slash    val
                 :star     "*"
                 :stmt     ["&" (strfirst val) " " (list2str (rest val)) " /"]
                 :string   val
                 :true     "t"
                 :uint     val
                 :value    (delegate val)
                 :values   (cjoin (delegate val))
                 :ws       ""
                 :wsopt    ""))))

(defn- nml-uniq [values]
  (loop [head (first values) tail (rest values) tree []]
    (if (nil? head)
      tree
      (let [name #(nml-name (second %))
            copy  (some #(= (name head) (name %)) tail)]
        (recur (first tail) (rest tail) (into tree (if copy [] [head])))))))

;; cli

(defn- assoc-g [m k v]
  (let [gets (:get m [])]
    (assoc m :get (into gets [v]))))

(defn- assoc-i [m k v]
  (if (k m) (fail (msgs :multi-in)))
  (assoc m k v))

(defn- assoc-o [m k v]
  (if (k m) (fail (msgs :multi-out)))
  (assoc m k v))

(defn- assoc-s [m k v]
  (let [sets (:set m [])]
    (assoc m :set (into sets [v]))))

(defn- parse-get [x]
  (string/split x #":" 2))

(defn- parse-set [x]
  (let [[nml+key val] (string/split x #"=" 2)
        [nml key] (parse-get nml+key)]
    [nml key val]))

(def cliopts
  [
   ["-c" "--create"    "Create new namelist"                                                                ]
   ["-g" "--get n:k"   "Get value of key 'k' in namelist 'n'"         :assoc-fn assoc-g :parse-fn parse-get ]
   ["-h" "--help"      "Show usage information"                                                             ]
   ["-i" "--in file"   "Input file (default: stdin)"                  :assoc-fn assoc-i                     ]
   ["-n" "--no-prefix" "Report values without 'namelist:key=' prefix"                                       ]
   ["-o" "--out file"  "Output file (default: stdout)"                :assoc-fn assoc-o                     ]
   ["-s" "--set n:k=v" "Set value of key 'k' in namelist 'n' to 'v'"  :assoc-fn assoc-s :parse-fn parse-set ]
   ["-v" "--version"   "Show version information"                                                           ]
   ])

;; nml public defns

(defn nml-get [tree nml key]
  (let [stmt     (last (filter #(= (nml-name %) (string/lower-case nml)) (rest tree)))
        nvseq    (let [x (last stmt)] (if (= (first x) :nvseq) x []))
        nvsubseq (last (filter #(= (nml-name %) (string/lower-case key)) (rest nvseq)))
        values   (last nvsubseq)]
    (if (nil? values) "" (nml-str values))))

(defn nml-set [tree nml key val & sub]
  (let [child  (if sub :nvsubseq :stmt)
        match  (if sub (string/lower-case key) (string/lower-case nml))
        parent (if sub :nvseq :s)
        proxy  (if sub (str match "=0") (str "&" match " /"))
        vnew   (if sub (fn [tree] (nml-parse val :values)) #(nml-set % nml key val true))
        f      (fn
                 ([k v] (if (= (nml-str k) match) [child k (vnew v  )] [child k v]))
                 ([k  ] (if (= (nml-str k) match) [child k (vnew nil)] [child k  ])))]
    (insta/transform {child f} (nml-add tree parent child match proxy))))

(defn nml-tree [s]
  (let [tree  (nml-parse s :s)
        child :nvseq
        f     (fn [& v] (into [child] (nml-uniq v)))]
      (insta/transform {child f} tree)))

;; formatting

(defn- fmt-namelist [nls-map]
  (let [f0 #(str "  " (first %) "=" (last %) "\n")
        f1 (fn [x] (str "&" (first x) "\n" (apply str (map f0 (sort (last x)))) "/\n"))]
    (string/trim (apply str (map f1 (sort nls-map))))))

;; main

(defn -main [& args]
  (alter-var-root #'*read-eval* (constantly false))
  (let [{:keys [options arguments summary]} (cli/parse-opts args cliopts)
        gets (:get options)
        sets (:set options)
        in   (or (:in  options) *in* )
        out  (or (:out options) *out*)]
    (if (not-empty arguments) (fail (str "Unexpected argument '" (first arguments) "'.")))
    (if (:help options) (usage summary))
    (if (:version options) (do (println version) (System/exit 0)))
    (if (and gets (:out options)) (fail (msgs :get+out)))
    (if (and gets sets) (fail (msgs :get+set)))
    (if (and sets (:no-prefix options)) (fail (msgs :set+no-prefix)))
    (if (and (:create options) (:in options)) (fail (msgs :create+in)))
    (let [tree (nml-tree
                (if (:create options)
                  ""
                  (try (slurp in)
                       (catch Exception e
                         (fail (str "Could not read from '" in "'."))))))
          nls-map (nml-map tree)]
      (cond gets  (nml-gets tree gets (:no-prefix options))
            sets  (let [s (nml-str (nml-sets tree sets))]
                    (if (= out *out*)
                      (println (string/trim s))
                      (try (spit out s)
                           (catch Exception e
                             (fail (str "Could not write to '" out "'."))))))
            :else (println (fmt-namelist nls-map))))))
