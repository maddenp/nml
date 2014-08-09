(ns nml.core-test
  (:require [clojure.test :refer :all]
            [nml.core :refer :all]))


(let [tree (nml-tree "test/nml/nl")]

  (deftest character
    (is (= (nml-get tree "na" "C0") "'foo'"   ))
    (is (= (nml-get tree "NA" "c1") "'bar'"   ))
    (is (= (nml-get tree "nA" "c2") "\"baz\"" )))

  (deftest integer
    (is (= (nml-get tree "nb" "I0") "88"      ))
    (is (= (nml-get tree "nb" "I1") "+88"     ))
    (is (= (nml-get tree "NB" "i2") "-88"     ))
    (is (= (nml-get tree "nb" "i3") ""        )))

  (deftest logical
    (is (= (nml-get tree "NC" "t0") "t"       ))
    (is (= (nml-get tree "NC" "t1") "t"       ))
    (is (= (nml-get tree "NC" "t2") "t"       ))
    (is (= (nml-get tree "NC" "t3") "t"       ))
    (is (= (nml-get tree "NC" "t4") "t"       ))
    (is (= (nml-get tree "NC" "t5") "t"       ))
    (is (= (nml-get tree "nc" "f0") "f"       ))
    (is (= (nml-get tree "nc" "f1") "f"       ))
    (is (= (nml-get tree "nc" "f2") "f"       ))
    (is (= (nml-get tree "nc" "f3") "f"       ))
    (is (= (nml-get tree "nc" "f4") "f"       ))
    (is (= (nml-get tree "nc" "f5") "f"       )))
  )
