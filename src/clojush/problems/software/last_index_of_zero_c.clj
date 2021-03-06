;; last_index_of_zero.clj
;; Nic McPhee, mcphee@morris.umn.edu
;;
;; Problem Source: iJava (http://ijava.cs.umass.edu/)
;;
;; Given a vector of integers of length <= 50, each integer in the range [-50,50],
;; at least one of which is 0, return the index of the last occurance of 0 in the vector.
;;
;; input stack has 1 input vector of integers

(ns clojush.problems.software.last-index-of-zero-c
  (:use clojush.pushgp.pushgp
        [clojush pushstate interpreter random util globals simplification]
        clojush.instructions.tag
        [clojure.math numeric-tower combinatorics]
        )
  (:require [clojush.problems.software.last-index-of-zero :as liz]))

(def exec-reuse-instrs '(exec_dup, exec_dup_times, exec_dup_items, exec_yankdup, exec_do*range, exec_do*count,  exec_do*times, exec_while, exec_do*while, exec_s, exec_y, exec_do*vector_integer, exec_do*vector_float, exec_do*vector_boolean, exec_do*vector_string))

; Atom generators
(def last-index-of-zero-atom-generators
  (concat (list
           ^{:generator-label "Random numbers in the range [-50,50]"}
           (fn [] (- (lrand-int 101) 50))
            ;;; end ERCs
           (tag-instruction-erc [:integer :boolean :vector_integer :exec] 1000)
           (tagged-instruction-erc 1000)
           (untag-instruction-erc 1000)
           (registered-for-type "return_")
            ;;; end tag ERCs
           'in1
            ;;; end input instructions
           )
          ;(remove (set exec-reuse-instrs) (registered-for-stacks [:integer :boolean :vector_integer :exec])  )
          (registered-for-stacks [:integer :boolean :vector_integer :exec])
          ))

;; Define test cases
(defn random-sequence-with-at-least-one-zero
  [max-extra-zeros max-additional-values]
  (shuffle
   (concat
    [0] ; To ensure at least one zero
    (repeat (lrand-int (inc max-extra-zeros)) 0)
    (repeatedly (lrand-int (inc max-additional-values)) #(- (lrand-int 101) 50)))))

;; A list of data domains for the problem. Each domain is a vector containing
;; a "set" of inputs and two integers representing how many cases from the set
;; should be used as training and testing cases respectively. Each "set" of
;; inputs is either a list or a function that, when called, will create a
;; random element of the set.
(def last-index-of-zero-data-domains
  [^{:domain-label "length 2 vectors"}
   [(list [0 1]
          [1 0]
          [7 0]
          [0 8]
          [0 -1]
          [-1 0]
          [-7 0]
          [0 -8]) 8 0]
   ^{:domain-label "vectors of all zeros"}
   [(map #(vec (repeat (inc %) 0)) (range 50)) 30 20]
   ^{:domain-label "permutations of a 4 item vector with one zero"}
   [(map vec (permutations [0 5 -8 9])) 20 4]
   ^{:domain-label "permutations of a 4 item vector with two zeros"}
   [(map vec (permutations [0 0 -8 9])) 10 2]
   ^{:domain-label "permutations of a 4 item vector with three zeros"}
   [(map vec (permutations [0 0 0 9])) 4 0]
   ^{:domain-label "random cases"}
   [(fn [] (random-sequence-with-at-least-one-zero 5 44)) 78 974]
   ])

;;Can make Last Index of Zero test data like this:
;(test-and-train-data-from-domains last-index-of-zero-data-domains)

; Helper function for error function
(defn last-index-of-zero-test-cases
  "Takes a sequence of inputs and gives IO test cases of the form
   [input output]."
  [inputs]
  (map #(vector % (.lastIndexOf % 0))
       inputs))

(defn make-last-index-of-zero-error-function-from-cases
  [train-cases test-cases]
  (fn the-actual-last-index-of-zero-error-function
    ([individual]
      (the-actual-last-index-of-zero-error-function individual :train))
    ([individual data-cases] ;; data-cases should be :train or :test
     (the-actual-last-index-of-zero-error-function individual data-cases false))
    ([individual data-cases print-outputs]
     (let [;stacks-depth (atom (zipmap push-types (repeat 0)))
           ;state-with-tags (tagspace-initialization (str (:program individual)) 1000 (make-push-state))
           reuse-metric (atom ())                           ;the lenght will be equal to the number of test cases
           repetition-metric (atom ())
           behavior (atom '())
           local-tagspace (case data-cases
                            :train                          ; (if global-use-lineage-tagspaces
                            ;(atom (:tagspace individual))
                            (atom @global-common-tagspace)
                            ; )
                            :simplify (atom (:tagspace individual)) ; during simplification, the tagspace should not be changed.
                            :test (atom (:tagspace individual))
                            ; (if global-use-lineage-tagspaces
                            ; (atom (:tagspace individual))
                              (atom @global-common-tagspace)
                              ;)
                              )
           update? (atom false)
           cases (case data-cases
                                    :train train-cases
                                    :simplify train-cases
                                    :test test-cases
                                    data-cases)
           errors (let [ran nil            ; (if (and (not= data-cases :test) (not= data-cases :simplify))
                                         ;(rand-nth cases)
                                         ; nil)
                                         ]
                                     (doall
                                       (for [[input correct-output] cases]
                                         (let [final-state (if (= [input correct-output] ran)
                                                             (run-push (:program
                                                                         ;(auto-simplify-lite individual
                                                                         ;                    (fn [inp] (liz/make-last-index-of-zero-error-function-from-cases inp nil)) ; error-function per test case
                                                                         ;                    75
                                                                         ;                    (first liz/last-index-of-zero-train-and-test-cases) ; cases
                                                                         ;                    false 100)
                                                                         individual
                                                                         )
                                                                       (push-item input :input
                                                                                  (assoc (make-push-state) :calculate-mod-metrics (= [input correct-output] ran))
                                                                                  ))
                                                             (run-push (:program individual)
                                                                       (->> (assoc (make-push-state) :tag @local-tagspace)
                                                                            ;(make-push-state)
                                                                            (push-item input :input))))
                                               result (top-item :integer final-state)
                                               _ (if (and (not= data-cases :test) (not= data-cases :simplify))
                                                   (reset! local-tagspace (get final-state :tag)))
                                               ]
                                           ; (when print-outputs
                                           ; (println (format "Correct output: %2d | Program output: %s"
                                           ;                correct-output
                                           ;                 (str result))))
                                           ;(doseq [[k v] (:max-stack-depth final-state)] (swap! stacks-depth update k #(max % v)))
                                           ; (if (= [input correct-output] ran)
                                           ; (let [metrics (mod-metrics (:trace final-state) (:trace_id final-state))]
                                           ; (do
                                           ;  (swap! reuse-metric conj (first metrics))
                                           ;  (swap! repetition-metric conj (last metrics)))))

                                           ; Record the behavior
                                           (swap! behavior conj result)
                                           ; Error is absolute distance from correct index
                                           (if (number? result)
                                             (abs (- result correct-output)) ; distance from correct integer
                                             1000000)       ; penalty for no return value
                                           ))))
                            errors-wct (comment             ;doall
                                         (for [[input correct-output] cases]
                                           (let [final-state (run-push (:program individual) ;'(tagged_0)
                                                                       (->> (make-push-state)
                                                                            ;(assoc (make-push-state) :tag @local-tagspace)
                                                                            (push-item input :input)))
                                                 result (top-item :integer final-state)
                                                 ]
                                             ; Record the behavior
                                             ;(swap! behavior conj result)
                                             ; Error is absolute distance from correct index
                                             (if (number? result)
                                               (abs (- result correct-output)) ; distance from correct integer
                                               1000000)     ; penalty for no return value
                                             )))
                            _ (if (and (not= data-cases :test) (not= data-cases :simplify))
                                (if (let [x (vec errors)
                                          ; _ (prn x)
                                          y (first (:history individual))
                                          ;y (vec errors-wct)
                                          ; _ (prn y)
                                          ]
                                      (if (nil? y)
                                        true
                                        ;(some? (some true? (map #(< %1 %2) x y))))) ;child is better than mom on at least one test case; can be worse on others
                                        (every? true? (map #(<= %1 %2) x y))
                                        ;(not= x y)
                                        ;true
                                        ))
                                  (do
                                    (reset! global-common-tagspace @local-tagspace)
                                    ; (reset! update? true)
                                    ; (prn @global-common-tagspace)
                                    )))
           ]
        (if (= data-cases :test)
          (assoc individual :test-errors errors)
          (assoc individual :behaviors @behavior :errors errors :reuse-info @reuse-metric :repetition-info @repetition-metric :tagspace @local-tagspace ; (let [ts (:tagspace individual)]
                            ; (if true                        ;update?
                            ;@local-tagspace
                            ;  ts))
                            ; :tagspace-effect (if update? 1 0)
                            ))
          ))))

(defn get-last-index-of-zero-train-and-test
  "Returns the train and test cases."
  [data-domains]
  (map last-index-of-zero-test-cases
       (test-and-train-data-from-domains data-domains)))

; Define train and test cases
(def last-index-of-zero-train-and-test-cases
  (get-last-index-of-zero-train-and-test last-index-of-zero-data-domains))

(defn last-index-of-zero-initial-report
  [argmap]
  (println "Train and test cases:")
  (doseq [[i case] (map vector (range) (first last-index-of-zero-train-and-test-cases))]
    (println (format "Train Case: %3d | Input/Output: %s" i (str case))))
  (doseq [[i case] (map vector (range) (second last-index-of-zero-train-and-test-cases))]
    (println (format "Test Case: %3d | Input/Output: %s" i (str case))))
  (println ";;******************************"))

(defn last-index-of-zero-report
  "Custom generational report."
  [best population generation error-function report-simplifications]
  (let [best-test-errors (:test-errors (error-function best :test))
        best-total-test-error (apply +' best-test-errors)]
    (println ";;******************************")
    (printf ";; -*- Last Index of Zero problem report - generation %s\n" generation)(flush)
    (println "Test total error for best:" best-total-test-error)
    (println (format "Test mean error for best: %.5f" (double (/ best-total-test-error (count best-test-errors)))))
    (when (zero? (:total-error best))
      (doseq [[i error] (map vector
                             (range)
                             best-test-errors)]
        (println (format "Test Case  %3d | Error: %s" i (str error)))))
    (println ";;------------------------------")
    (println "Outputs of best individual on training cases:")
    (error-function best :train true)
    (println ";;******************************")
    )) ;; To do validation, could have this function return an altered best individual
       ;; with total-error > 0 if it had error of zero on train but not on validation
       ;; set. Would need a third category of data cases, or a defined split of training cases.


; Define the argmap
(def argmap
  {:error-function (make-last-index-of-zero-error-function-from-cases (first last-index-of-zero-train-and-test-cases)
                                                                      (second last-index-of-zero-train-and-test-cases))
   :atom-generators last-index-of-zero-atom-generators
   :max-points 1200
   :max-genome-size-in-initial-program 150
   :evalpush-limit 600
   :population-size 1000
   :max-generations 300
   :parent-selection :lexicase
   :downsample-factor 0.5
   :training-cases (first last-index-of-zero-train-and-test-cases)
   :genetic-operator-probabilities {:uniform-addition-and-deletion 1}
   :uniform-addition-and-deletion-rate 0.09
   ;:genetic-operator-probabilities {:alternation 0.2
   ;                                 :uniform-mutation 0.2
   ;                                 :uniform-close-mutation 0.1
   ;                                 [:alternation :uniform-mutation] 0.5
   ;                                 }
   ;:alternation-rate 0.01
   ;:alignment-deviation 10
   ;:uniform-mutation-rate 0.01
   :problem-specific-report last-index-of-zero-report
   :problem-specific-initial-report last-index-of-zero-initial-report
   :report-simplifications 0
   :final-report-simplifications 5000
   :max-error 1000000
   :meta-error-categories [:tag-usage]
   :use-single-thread true
   :print-history true
   :use-lineage-tagspaces false
   :pop-when-tagging false
   ;:tag-enrichment-types [:integer :boolean :vector_integer :exec]
   ;:tag-enrichment 50
   })
