(ns nml.core-test
  (:require [clojure.test :refer :all]
            [nml.core :refer :all]))


(let [tree (nml-tree "test/nml/nl")]

  (deftest get-string
    (is (= (nml-get tree "na" "C0") "'foo'"                         ))
    (is (= (nml-get tree "NA" "c1") "'BAR'"                         ))
    (is (= (nml-get tree "nA" "c2") "\"baz\""                       ))
    (is (= (nml-get tree "Na" "C3") "'qu\nx'"                       ))
    (is (= (nml-get tree "Na" "c4") "'a','b'"                       ))
    (is (= (nml-get tree "Na" "c5") "'c','d'"                       )))

  (deftest get-integer
    (is (= (nml-get tree "nb" "I0") "88"                            ))
    (is (= (nml-get tree "nb" "I1") "+88"                           ))
    (is (= (nml-get tree "NB" "i2") "-88"                           ))
    (is (= (nml-get tree "nb" "i3") ""                              )))

  (deftest get-logical
    (is (= (nml-get tree "NC" "t0") "t"                             ))
    (is (= (nml-get tree "NC" "t1") "t"                             ))
    (is (= (nml-get tree "NC" "t2") "t"                             ))
    (is (= (nml-get tree "NC" "t3") "t"                             ))
    (is (= (nml-get tree "NC" "t4") "t"                             ))
    (is (= (nml-get tree "NC" "t5") "t"                             ))
    (is (= (nml-get tree "nc" "f0") "f"                             ))
    (is (= (nml-get tree "nc" "f1") "f"                             ))
    (is (= (nml-get tree "nc" "f2") "f"                             ))
    (is (= (nml-get tree "nc" "f3") "f"                             ))
    (is (= (nml-get tree "nc" "f4") "f"                             ))
    (is (= (nml-get tree "nc" "f5") "f"                             )))

  (deftest get-complex
    (is (= (nml-get tree "nd" "C0") "(1.,2.)"                       ))
    (is (= (nml-get tree "nd" "C1") "(1.1,2.2)"                     )))

  (deftest get-real
    (is (= (nml-get tree "ne" "r0") "1.,1.0,-1.0,+1.0"              ))
    (is (= (nml-get tree "ne" "r1") "1.1e2,1.1d2,1.1e-2,1.1d+2"     ))
    (is (= (nml-get tree "ne" "r2") "-1.1e2,+1.1e2,-1.1d-2,+1.1d+2" )))
  )
