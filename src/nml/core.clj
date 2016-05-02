(ns nml.core
  (:require [clojure.string    :as string])
  (:require [instaparse.core   :as insta ])
  (:require [clojure.tools.cli :as cli   ])
  (:gen-class))

(declare nml-get nml-parse nml-set strmap valstr)

;; formatting

(defn- fmt-sh [m lo]
  (let [ms  (sort m)
        n   (lo 1)
        k   (lo 2)
        eq #(string/replace % "\"" "\\\"")
        f0  (fn [[dataref vals]]
              (let [v (eq vals)]
                (str "'" dataref "') echo \"" v "\";;")))
        f1  (fn [[name nv_sequence]]
              (let [x (strmap f0 nv_sequence)]
                (str "'" name "') case " k " in " x "*) echo '';;esac;;" )))]
    (str "nmlquery(){ case " n " in " (strmap f1 ms) "*) echo '';;esac; }\n")))

(defn- fmt-bash [m]
  (fmt-sh m #(str "\"${" % ",,}\"")))

(defn- fmt-ksh [m]
  (fmt-sh m #(str "\"$(echo $" % " | tr [:upper:] [:lower:])\"")))

(defn- fmt-namelist [m]
  (let [f0 (fn [[dataref vals]] (str "  " dataref "=" (valstr vals) "\n"))
        f1 (fn [[name nv_sequence]] (str "&" name "\n" (strmap f0 (sort nv_sequence)) "/\n"))]
    (strmap f1 (sort m))))

;; defs

(def formats
  {"bash"     fmt-bash
   "ksh"      fmt-ksh
   "namelist" fmt-namelist})

(def msgs
  {:bad-format    "Bad output format"
   :edit+in       "-i/--in not valid with -e/--edit"
   :edit+out      "-o/--out not valid with -e/--edit"
   :get+edit      "-e/--edit not valid with -g/--get"
   :get+format    "-f/--format not valid with -g/--get"
   :get+set       "-g/--get and -s/--set may not be mixed"
   :multi-edit    "-e/--edit may be specified only once"
   :multi-format  "-f/--format may be specified only once"
   :multi-in      "-i/--in may be specified only once"
   :multi-out     "-o/--out may be specified only once"
   :set+no-prefix "-n/--no-prefix not valid with -s/--set"})

(def parse (insta/parser (clojure.java.io/resource "grammar")))

(def version "0.3")

;; utility defns

(defn- fail [& lines]
  (binding [*out* *err*]
    (doseq [line lines]
      (println (str "nml: " line)))
    (System/exit 1)))

(defn- read-file [in]
  (try (slurp in)
       (catch Exception e
         (fail (str "Could not read from '" in "'")))))

(defn- strmap [f coll]
  (apply str (map f coll)))

(defn- usage [summary]
  (let [f (str "Valid output formats are: " (string/join ", " (keys formats)))]
    (doseq [x ["\nUsage: nml [options]\n\nOptions:\n" summary "" f ""]]
      (println x))
    (System/exit 0)))

(defn- valstr [vals]
  (string/join "," vals))

;; nml private defns

(defn- nml-gets [m gets no-prefix]
  (let [f (fn [[nml key]]
            (let [val (nml-get m nml key)]
              (if (= "" val) (fail (str nml ":" key " not found")))
              (if no-prefix val (str nml ":" key "=" val))))]
    (str (string/join "\n" (map f gets)) "\n")))

(defn- nml-out [out s]
  (if (= out *out*)
    (println (string/trim s))
    (try (spit out s)
         (catch Exception e
           (fail (str "Could not write to '" out "'"))))))

(defn- nml-parse [text start-symbol provenance]
;;   (let [unhide :all
;;         parses (insta/parses parse text :start start-symbol :unhide unhide)]
;;     (binding [*out* *err*]
;;       (doseq [parse parses] (println (str "----\n" parse)))
;;       (println (str "### " (count parses)))))
  (let [result (parse text :start start-symbol)]
    (if (insta/failure? result)
      (let [{t :text l :line c :column} result]
        (fail (str "Error parsing " provenance " at line " l " column " c ":")
              t
              (str (apply str (repeat (- c 1) " ")) "^")))
      result)))

(defn- nml-sets [m sets]
  (loop [m m s sets]
    (if (empty? s)
      m
      (let [[nml key val] (first s)]
        (if (nil? val) (fail (str "No value supplied for key '" key "'")))
        (recur (nml-set m nml key val) (rest s))))))

;; nml public defns

(defn nml-get [m nml key]
  (valstr (get (get m (string/lower-case nml) {}) (string/lower-case key) "")))

(defn nml-map [text start-symbol provenance]
  (let [tree (nml-parse text start-symbol provenance)
        blank (fn [& _] "")
        string_id (fn [& components] (apply str components))
        string_lc (fn [& components] (string/lower-case (apply string_id components)))]
    (let [new (insta/transform
               {
                :c identity
                :comma identity
                :complex string_id
                :dataref string_id
                :dec (fn [point & int] (str point (apply str int)))
                :exp string_lc
                :false string_lc
                :input_stmt_prefix string_id
                :int string_id
                :logical identity
                :minus identity
                :name string_lc
                :nv_subseq (fn [dataref & vals] {dataref vals})
                :nv_subseqs (fn [& nv_subseqs] (into {} nv_subseqs))
                :group_name string_lc
                :input_stmt (fn [group_name nv_subseqs] (into {} {group_name nv_subseqs}))
                :partref identity
                :plus identity
                :r identity
                :real string_id
                :s (fn [& input_stmts] (into {} input_stmts))
                :sign identity
                :star identity
                :string string_id
                :true string_lc
                :uint identity
                :user_supplied_vals (fn [& vals] (apply vector vals))
                :val string_id
                :val_and_sep identity
                } tree)]
      new)))

(defn nml-set [m nml key val]
  (let [val (nml-map val :user_supplied_vals "user-supplied value(s)")]
    (assoc-in m [(string/lower-case nml) (string/lower-case key)] val)))

;; cli

(defn- assoc-e [m k v]
  (if (k m) (fail (msgs :multi-edit)))
  (assoc m k v))

(defn- assoc-f [m k v]
  (if (k m) (fail (msgs :multi-format)))
  (assoc m k v))

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

(defn- parse-f [x]
  (if-not (contains? formats x) (fail (msgs :bad-format)))
  x)

(defn- parse-g [x]
  (string/split x #":" 2))

(defn- parse-s [x]
  (let [[nml+key val] (string/split x #"=" 2)
        [nml key] (parse-g nml+key)]
    [nml key val]))

(def cliopts
  [["-c" "--create"     "Create new namelist"                                                              ]
   ["-e" "--edit file"  "Edit file (instead of '-i file -o file')"     :assoc-fn assoc-e                   ]
   ["-f" "--format fmt" "Output in format 'fmt' (default: namelist)"   :assoc-fn assoc-f :parse-fn parse-f ]
   ["-g" "--get n:k"    "Get value of key 'k' in namelist 'n'"         :assoc-fn assoc-g :parse-fn parse-g ]
   ["-h" "--help"       "Show usage information"                                                           ]
   ["-i" "--in file"    "Input file (default: stdin)"                  :assoc-fn assoc-i                   ]
   ["-n" "--no-prefix"  "Report values without 'namelist:key=' prefix"                                     ]
   ["-o" "--out file"   "Output file (default: stdout)"                :assoc-fn assoc-o                   ]
   ["-s" "--set n:k=v"  "Set value of key 'k' in namelist 'n' to 'v'"  :assoc-fn assoc-s :parse-fn parse-s ]
   ["-v" "--version"    "Show version information"                                                         ]])

;; main

(defn -main [& args]

  (alter-var-root #'*read-eval* (constantly false))

  ;; bindings

  (let [{:keys [options arguments summary]} (cli/parse-opts args cliopts)
        gets       (:get options)
        sets       (:set options)
        edit       (:edit options)
        in         (or edit (:in options) *in*)
        out        (or edit (:out options) *out*)
        provenance (or edit (:in options) "stdin")]

    ;; error checking

    (if (not-empty arguments) (fail (str "Unexpected argument '" (first arguments) "'")))
    (if (:help options) (usage summary))
    (if (:version options) (do (println version) (System/exit 0)))
    (if (and gets sets) (fail (msgs :get+set)))
    (if (and gets edit) (fail (msgs :get+edit)))
    (if (and edit (:in options)) (fail (msgs :edit+in)))
    (if (and edit (:out options)) (fail (msgs :edit+out)))
    (if (and gets (:format options)) (fail (msgs :get+format)))
    (if (and sets (:no-prefix options)) (fail (msgs :set+no-prefix)))
    (if (and (:create options) (:in options)) (fail (msgs :create+in)))

    ;; read -> parse -> lookup or modify -> output

    (let [fmt (let [f (:format options)] (if f (formats f) fmt-namelist))
          m   (nml-map (if (:create options) "" (read-file in)) :s provenance)]
      (cond gets  (nml-out out (nml-gets m gets (:no-prefix options)))
            sets  (nml-out out (fmt (nml-sets m sets)))
            :else (nml-out out (fmt m))))))
