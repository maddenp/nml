(ns nml.core
  (:gen-class)
  (:require [instaparse.core :as insta ])
  (:require [clojure.string  :as string]))

(def nml (insta/parser (clojure.java.io/resource "grammar")))

(defn s [x]
  (let [k (first x)
        v (rest  x)
        cjoin    #(string/join "," %)
        delegate #(map s %)
        list2str #(apply str (map s %))
        sf       #(s (first %))
        sl       #(s (last %))]
;;   (println (str "k=" k " v=" v))
    (apply str
           (case k
             :array    [(sf v) (sl v)]
             :c        (sf v)
             :colon    ":"
             :comment  ""
             :complex  ["(" (cjoin (delegate v)) ")"]
             :dataref  (delegate v)
             :dec      (delegate v)
             :dot      "."
             :false    "f"
             :int      (delegate v)
             :logical  (sf v)
             :name     (map string/lower-case v)
             :nvchoice (sf v)
             :nvpair   (delegate v)
             :nvseq    (delegate v)
             :nvsubseq ["  " (sf v) "=" (sl v) "\n"]
             :partref  (sf v)
             :percent  "%"
             :r        (sf v)
             :real     (delegate v)
             :s        (delegate v)
             :sect     ["(" (list2str v) ")"]
             :sign     v
             :star     "*"
             :stmt     ["&" (sf v) "\n" (list2str (rest v)) "/\n"]
             :string   v
             :true     "t"
             :uint     v
             :value    (delegate v)
             :values   (cjoin (delegate v))))))

(defn -main
  [& args]
  (alter-var-root #'*read-eval* (constantly false))
  (println (s (nml "a"))))
