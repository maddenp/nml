(ns nml.core-test
  (:require [clojure.test :refer :all]
            [nml.core :refer :all]))


(let [tree (nml-tree (slurp "test/nml/nl"))]

  (deftest get-string
    (is (= (nml-get tree "na" "C0") "'foo'"                                         ))
    (is (= (nml-get tree "NA" "c1") "'BAR'"                                         ))
    (is (= (nml-get tree "nA" "c2") "\"baz\""                                       ))
    (is (= (nml-get tree "Na" "C3") "'qu\nx'"                                       ))
    (is (= (nml-get tree "Na" "c4") "'a','b'"                                       ))
    (is (= (nml-get tree "Na" "c5") "'c','d'"                                       ))
    (is (= (nml-get tree "Na" "x0") ""                                              )))

  (deftest get-integer
    (is (= (nml-get tree "nb" "I0") "88"                                            ))
    (is (= (nml-get tree "nb" "I1") "+88"                                           ))
    (is (= (nml-get tree "NB" "i2") "-88"                                           ))
    (is (= (nml-get tree "nb" "i3") ""                                              )))

  (deftest get-logical
    (is (= (nml-get tree "NC" "t0") "t"                                             ))
    (is (= (nml-get tree "NC" "t1") "t"                                             ))
    (is (= (nml-get tree "NC" "t2") "t"                                             ))
    (is (= (nml-get tree "NC" "t3") "t"                                             ))
    (is (= (nml-get tree "NC" "t4") "t"                                             ))
    (is (= (nml-get tree "NC" "t5") "t"                                             ))
    (is (= (nml-get tree "nc" "f0") "f"                                             ))
    (is (= (nml-get tree "nc" "f1") "f"                                             ))
    (is (= (nml-get tree "nc" "f2") "f"                                             ))
    (is (= (nml-get tree "nc" "f3") "f"                                             ))
    (is (= (nml-get tree "nc" "f4") "f"                                             ))
    (is (= (nml-get tree "nc" "f5") "f"                                             )))

  (deftest get-complex
    (is (= (nml-get tree "nd" "C0") "(1.,2.)"                                       ))
    (is (= (nml-get tree "nd" "C1") "(1.1,2.2)"                                     )))

  (deftest get-real
    (is (= (nml-get tree "ne" "r0") "1.,1.0,-1.0,+1.0"                              ))
    (is (= (nml-get tree "ne" "r1") "1.1e2,1.1d2,1.1e-2,1.1d+2"                     ))
    (is (= (nml-get tree "ne" "r2") "-1.1e2,+1.1e2,-1.1d-2,+1.1d+2"                 )))

  (deftest get-empty
    (is (= (nml-get tree "nf" "x0") ""                                              ))
    (is (= (nml-get tree "zz" "x0") ""                                              )))

  (deftest set-string
    (is (= (nml-get (nml-set tree "na" "c0" "'FOO'") "na" "c0") "'FOO'"             ))
    (is (= (nml-get (nml-set tree "na" "x0" "'FOO'") "na" "x0") "'FOO'"             ))
    (is (= (nml-get (nml-set tree "zz" "x0" "'FOO'") "zz" "x0") "'FOO'"             )))

  (deftest set-integer
    (is (= (nml-get (nml-set tree "nb" "i0" "99") "nb" "i0") "99"                   ))
    (is (= (nml-get (nml-set tree "nb" "x0" "1, +2 -3") "nb" "x0") "1,+2,-3"        ))
    (is (= (nml-get (nml-set tree "nb" "x0" "1\n2") "nb" "x0") "1,2"                )))

  (deftest set-logical
    (is (= (nml-get (nml-set tree "nc" "f0" "T,F") "nc" "f0") "t,f"                 ))
    (is (= (nml-get (nml-set tree "nc" "f0" ".false.,.true.") "nc" "f0") "f,t"      ))
    (is (= (nml-get (nml-set tree "nc" "x0" ".t1234") "nc" "x0") "t"                ))
    (is (= (nml-get (nml-set tree "ZZ" "F0" ".fasdf") "zz" "f0") "f"                )))

  (deftest set-complex
    (is (= (nml-get (nml-set tree "ND" "C0" "(3.14,2.18)") "nd" "c0") "(3.14,2.18)" )))

  (deftest set-real
    (is (= (nml-get (nml-set tree "NE" "R0" "+314.e-2") "ne" "r0") "+314.e-2"       ))))
