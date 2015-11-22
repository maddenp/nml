(ns nml.core-test
  (:require [clojure.test :refer :all]
            [nml.core :refer :all]))


(let [m (nml-map (slurp "test/nml/nl") :s "test")]

  (deftest gets
    (is (= (nml-get m "na" "C0") "'foo'"                                         ))
    (is (= (nml-get m "NA" "c1") "'BAR'"                                         ))
    (is (= (nml-get m "nA" "c2") "\"baz\""                                       ))
    (is (= (nml-get m "Na" "C3") "'/ &'"                                         ))
    (is (= (nml-get m "Na" "c4") "'a','b'"                                       ))
    (is (= (nml-get m "Na" "c5") "'c','d'"                                       ))
    (is (= (nml-get m "Na" "c6") "3*'z'"                                         ))
    (is (= (nml-get m "Na" "c7") "3*"                                            ))
    (is (= (nml-get m "Na" "x0") ""                                              ))
    (is (= (nml-get m "nb" "I0") "88"                                            ))
    (is (= (nml-get m "nb" "I1") "+88"                                           ))
    (is (= (nml-get m "NB" "i2") "-88"                                           ))
    (is (= (nml-get m "nb" "i3") ""                                              ))
    (is (= (nml-get m "NC" "t0") "t"                                             ))
    (is (= (nml-get m "NC" "t1") "t"                                             ))
    (is (= (nml-get m "NC" "t2") "t"                                             ))
    (is (= (nml-get m "NC" "t3") "t"                                             ))
    (is (= (nml-get m "NC" "t4") "t"                                             ))
    (is (= (nml-get m "NC" "t5") "t"                                             ))
    (is (= (nml-get m "nc" "f0") "f"                                             ))
    (is (= (nml-get m "nc" "f1") "f"                                             ))
    (is (= (nml-get m "nc" "f2") "f"                                             ))
    (is (= (nml-get m "nc" "f3") "f"                                             ))
    (is (= (nml-get m "nc" "f4") "f"                                             ))
    (is (= (nml-get m "nc" "f5") "f"                                             ))
    (is (= (nml-get m "nd" "M0") "(1.,2.)"                                       ))
    (is (= (nml-get m "nd" "M1") "(1.1,2.2)"                                     ))
    (is (= (nml-get m "ne" "r0") "1.,1.0,-1.0,+1.0"                              ))
    (is (= (nml-get m "ne" "r1") "1.1e2,1.1d2,1.1e-2,1.1d+2"                     ))
    (is (= (nml-get m "ne" "r2") "-1.1e2,+1.1e2,-1.1d-2,+1.1d+2"                 ))
    (is (= (nml-get m "ne" "r3") ","                                             ))
    (is (= (nml-get m "nf" "x0") ""                                              ))
    (is (= (nml-get m "zz" "x0") ""                                              )))

  (deftest sets
    (is (= (nml-get (nml-set m "na" "c0" "'FOO'") "na" "c0") "'FOO'"             ))
    (is (= (nml-get (nml-set m "na" "x0" "'FOO'") "na" "x0") "'FOO'"             ))
    (is (= (nml-get (nml-set m "zz" "x0" "'FOO'") "zz" "x0") "'FOO'"             ))
    (is (= (nml-get (nml-set m "nb" "i0" "99") "nb" "i0") "99"                   ))
    (is (= (nml-get (nml-set m "nb" "x0" "1, +2 -3") "nb" "x0") "1,+2,-3"        ))
    (is (= (nml-get (nml-set m "nb" "x0" "1\n2") "nb" "x0") "1,2"                ))
    (is (= (nml-get (nml-set m "nc" "f0" "T,F") "nc" "f0") "t,f"                 ))
    (is (= (nml-get (nml-set m "nc" "f0" ".false.,.true.") "nc" "f0") "f,t"      ))
    (is (= (nml-get (nml-set m "nc" "x0" ".t1234") "nc" "x0") "t"                ))
    (is (= (nml-get (nml-set m "ZZ" "F0" ".fasdf") "zz" "f0") "f"                ))
    (is (= (nml-get (nml-set m "ND" "M0" "(3.14,2.18)") "nd" "m0") "(3.14,2.18)" ))
    (is (= (nml-get (nml-set m "NE" "R0" "+314.e-2") "ne" "r0") "+314.e-2"       ))))
