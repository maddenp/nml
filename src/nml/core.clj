(ns nml.core
  (:require [clojure.data.json :as json ]
            [clojure.java.io   :as io   ]
            [clojure.string    :as s    ]
            [clojure.tools.cli :as cli  ]
            [clojure.walk      :as walk ]
            [instaparse.core   :as insta])
  (:gen-class))

(defn fail
  [& lines]
  (binding [*out* *err*]
    (doseq [line lines]
      (println (str "nml: " line)))
    (System/exit 1)))

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

(defn assoc-e
  [m k v]
  (when (k m) (fail (msgs :multi-edit)))
  (assoc m k v))

(defn assoc-f
  [m k v]
  (when (k m) (fail (msgs :multi-format)))
  (assoc m k v))

(defn assoc-g
  [m k v]
  (let [gets (:get m [])]
    (assoc m :get (into gets [v]))))

(defn assoc-i
  [m k v]
  (when (k m) (fail (msgs :multi-in)))
  (assoc m k v))

(defn assoc-o
  [m k v]
  (when (k m) (fail (msgs :multi-out)))
  (assoc m k v))

(defn assoc-s
  [m k v]
  (let [sets (:set m [])]
    (assoc m :set (into sets [v]))))

(defn valstr
  [vals]
  (s/join "," vals))

(defn strmap
  [f coll]
  (apply str (map f coll)))

(defn fmt-sh
  [m]
  (let [lo #(str "\"$(echo $" % " | tr [:upper:] [:lower:])\"")
        esc-quotes #(s/replace % "\"" "\\\"")
        f0 (fn [[dataref vals]]
             (let [v (esc-quotes (valstr vals))]
               (str "'" dataref "') echo \"" v "\";;")))
        f1 (fn [[name nv-sequence]]
             (let [x (strmap f0 nv-sequence)]
               (str "'" name "') case " (lo 2) " in " x "*) echo '';;esac;;" )))]
    (str "nmlquery(){ case " (lo 1) " in " (strmap f1 (sort m)) "*) echo '';;esac; }\n")))

(defn fmt-bash
  [m]
  (fmt-sh m))

(defn fmt-json
  [m]
  (let [f (fn [e]
            (let [re #"[+-]?(\d*\.)?\d+(d[+-]?\d+)?"
                  e (if (and (string? e) (re-matches re e)) (s/replace e #"d" "e") e)
                  x (try (read-string e) (catch Exception e nil))]
              (cond
                (number? x) x
                (= "t" e) true
                (= "f" e) false
                (and (seq? e) (= 1 (count e))) (first e)
                (string? e) (let [m (re-matches #"^[\'\"](.*)[\'\"]$" e)]
                              (if m (second m) e))
                :else e)))]
    (with-out-str (json/pprint (walk/postwalk f m)))))

(defn fmt-ksh
  [m]
  (fmt-sh m))

(defn fmt-namelist
  [sort? m]
  (let [f0 (fn [[dataref vals]] (str "  " dataref "=" (valstr vals) "\n"))
        f1 (fn [[name nv-sequence]] (str "&" name "\n" (strmap f0 (sort nv-sequence)) "/\n"))]
    (strmap f1 (if sort? (sort m) m))))

(def formats
  {"bash"     fmt-bash
   "json"     fmt-json
   "ksh"      fmt-ksh
   "namelist" fmt-namelist})

(defn parse-f
  [x]
  (when-not (contains? formats x) (fail (msgs :bad-format)))
  x)

(defn parse-g
  [x]
  (s/split x #":" 2))

(defn parse-s
  [x]
  (let [[nml+key val] (s/split x #"=" 2)
        [nml key] (parse-g nml+key)]
    [nml key val]))

(def cliopts
  [["-c" "--create"     "Create new namelist file"                                                         ]
   ["-e" "--edit file"  "Edit file (instead of '-i file -o file')"     :assoc-fn assoc-e                   ]
   ["-f" "--format fmt" "Output in format 'fmt' (default: namelist)"   :assoc-fn assoc-f :parse-fn parse-f ]
   ["-g" "--get n:k"    "Get value of key 'k' in namelist 'n'"         :assoc-fn assoc-g :parse-fn parse-g ]
   ["-h" "--help"       "Show usage information"                                                           ]
   ["-i" "--in file"    "Input file (default: stdin)"                  :assoc-fn assoc-i                   ]
   ["-k" "--keep-order" "Keep namelists in original order"                                                 ]
   ["-n" "--no-prefix"  "Report values without 'namelist:key=' prefix"                                     ]
   ["-o" "--out file"   "Output file (default: stdout)"                :assoc-fn assoc-o                   ]
   ["-s" "--set n:k=v"  "Set value of key 'k' in namelist 'n' to 'v'"  :assoc-fn assoc-s :parse-fn parse-s ]
   ["-v" "--version"    "Show version information"                                                         ]])

(defn usage
  [summary]
  (let [f (str "Valid output formats are: " (s/join ", " (keys formats)))]
    (doseq [x ["\nUsage: nml [options]\n\nOptions:\n" summary "" f ""]]
      (println x))
    (System/exit 0)))

(def parse (insta/parser (io/resource "grammar")))

(defn nml-parse
  [text start-symbol provenance]
  ;; TEST FOR AMBIGUOUS GRAMMAR
  #_(let [unhide :all
          parses (insta/parses parse text :start start-symbol :unhide unhide)]
      (binding [*out* *err*]
        (doseq [parse parses] (println (str "----\n" parse)))
        (println (str "### " (count parses)))))
  (let [result (parse text :start start-symbol)]
    (if (insta/failure? result)
      (let [{t :text l :line c :column} result]
        (fail (str "Error parsing " provenance " at line " l " column " c ":")
              t
              (str (apply str (repeat (- c 1) " ")) "^")))
      result)))

(defn nml-map
  [text start-symbol provenance]
  (let [tree (nml-parse text start-symbol provenance)
        blank (fn [& _] "")
        string-id (fn [& components] (apply str components))
        string-lc (fn [& components] (s/lower-case (apply string-id components)))]
    (let [new (insta/transform
               {:array string-id
                :c identity
                :comma identity
                :complex string-id
                :dataref string-id
                :dec (fn [point & int] (str point (apply str int)))
                :exp string-lc
                :false string-lc
                :input-stmt-prefix string-id
                :int string-id
                :logical identity
                :minus identity
                :name string-lc
                :nv-subseq (fn [dataref & vals] {dataref vals})
                :nv-subseqs (fn [& nv-subseqs] (into {} nv-subseqs))
                :group-name string-lc
                :input-stmt (fn [group-name nv-subseqs] {group-name nv-subseqs})
                :partref identity
                :plus identity
                :r identity
                :real string-id
                :s (fn [& input-stmts] (apply array-map (flatten (map seq input-stmts))))
                :sect string-id
                :sign identity
                :star identity
                :string string-id
                :true string-lc
                :uint identity
                :user-supplied-vals (fn [& vals] (apply vector vals))
                :val string-id
                :val-and-sep identity} tree)]
      new)))

(defn read-file
  [in]
  (try (slurp in)
       (catch Exception e
         (fail (str "Could not read from '" in "'")))))

(defn nml-out
  [out s]
  (if (= out *out*)
    (println (s/trim s))
    (try (spit out s)
         (catch Exception e
           (fail (str "Could not write to '" out "'"))))))

(defn nml-get
  [m nml key]
  (valstr (get (get m (s/lower-case nml) {}) (s/lower-case key) "")))

(defn nml-gets
  [m gets no-prefix]
  (let [f (fn [[nml key]]
            (let [val (nml-get m nml key)]
              (when (= "" val) (fail (str nml ":" key " not found")))
              (if no-prefix val (str nml ":" key "=" val))))]
    (str (s/join "\n" (map f gets)) "\n")))

(defn nml-set
  [m nml key val]
  (let [val (nml-map val :user-supplied-vals "user-supplied value(s)")]
    (assoc-in m [(s/lower-case nml) (s/lower-case key)] val)))

(defn nml-sets
  [m sets]
  (loop [m m s sets]
    (if (empty? s)
      m
      (let [[nml key val] (first s)]
        (when (nil? val) (fail (str "No value supplied for key '" key "'")))
        (recur (nml-set m nml key val) (rest s))))))

(defn -main
  [& args]

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

    (when (not-empty arguments) (fail (str "Unexpected argument '" (first arguments) "'")))
    (when (:help options) (usage summary))
    (when (:version options) (do (println (s/trim-newline (slurp (io/resource "version"))))
                                 (System/exit 0)))
    (when (and gets sets) (fail (msgs :get+set)))
    (when (and gets edit) (fail (msgs :get+edit)))
    (when (and edit (:in options)) (fail (msgs :edit+in)))
    (when (and edit (:out options)) (fail (msgs :edit+out)))
    (when (and gets (:format options)) (fail (msgs :get+format)))
    (when (and sets (:no-prefix options)) (fail (msgs :set+no-prefix)))
    (when (and (:create options) (:in options)) (fail (msgs :create+in)))

    ;; read -> parse -> lookup or modify -> output

    (let [fmt-namelist (partial fmt-namelist (not (:keep-order options)))
          formats      (assoc formats "namelist" fmt-namelist)
          fmt          (get formats (:format options) fmt-namelist)
          m            (if (:create options) (array-map) (nml-map (read-file in) :s provenance))]
      (cond gets  (nml-out out (nml-gets m gets (:no-prefix options)))
            sets  (nml-out out (fmt (nml-sets m sets)))
            :else (nml-out out (fmt m))))))
