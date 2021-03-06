;; Clojush tutorial code

;; Evaluate this file and then uncomment and evaluate one form at a time.

;; Declare a namespace for this file, with access to the clojush.ns namespace
;; (which will allow us to easily use all of clojush) and clojure.math.numeric-tower
;; (which provides access to an "abs" function that we use in an example below.

(ns clojush.problems.demos.tutorial
  (:use [clojush.ns]
        [clojure.math.numeric-tower]
        [clojush.individual :as individual]))

;; Get access to all clojush namespaces (except for examples/* and experimental/*)

(use-clojush)

;; Adding numbers in clojure:

;(+ 1 2)

;; Adding numbers in push:

;(run-push '(1 2 integer_add) 
;          (make-push-state))

;; Providing true as a third argument produces a trace of all stacks as it runs:

(run-push '(exec_dup (exec_swap 1 2))
          ;(make-push-state)
          (assoc (make-push-state) :calculate-mod-metrics true)
          true true true
          )          


(comment
  ; Some example programs and their exec traces
;prog1 = "(exec_dup (1 2 3 exec_swap 4 5 6))"
;#there is no repetition in the following trace. Hence it should b easier to calclate modularity for this.
;trace1 = "((exec_dup (1 2 3 exec_swap 4 5 6)) exec_dup (1 2 3 exec_swap 4 5 6) 1 2 3 exec_swap 5 4 6 (1 2 3 exec_swap 4 5 6) 1 2 3 exec_swap 5 4 6)"

;#example 2

;prog2 = "((1 2 exec_swap 3 4) 5 6)"
;trace2 = "(((1 2 exec_swap 3 4) 5 6) (1 2 exec_swap 3 4) 1 2 exec_swap 4 3 5 6)"

;#example 3
;prog3 = "(exec_dup (1 2) 3 4)"
;trace3 = "((exec_dup (1 2) 3 4) exec_dup (1 2) 1 2 (1 2) 1 2 3 4)"
;trace_id3 = "((0 (1 2) 3 4) 0 (1 2) 1 2 (1 2) 1 2 3 4)"

;prog4 =  "(exec_dup (exec_swap 1 2))"
;trace4 = "((exec_dup (exec_swap 1 2)) exec_dup (exec_swap 1 2) exec_swap 2 1 (exec_swap 1 2) exec_swap 2 1)"
;trace_id4 = "((1 (2 3 4)) 1 (2 3 4) 2 4 3 (2 3 4) 2 4 3)"
  
  )
;;;;;;;;;;;;
;; Integer symbolic regression of x^3 - 2x^2 - x (problem 5 from the 
;; trivial geography chapter) with minimal integer instructions and an 
;; input instruction that uses the default input stack

(def argmap
  {:error-function (fn [individual]
                     (assoc individual
                            :errors
                            (doall
                             (for [input (range 10)]
                               (let [state (->> (make-push-state)
                                                (push-item input :integer)
                                                (push-item input :input)
                                                (run-push (:program individual)))
                                     top-int (top-item :integer state)]
                                 (if (number? top-int)
                                   (abs (- top-int 
                                           (- (* input input input) 
                                              (* 2 input input)
                                              input)))
                                   1000))))))
   :atom-generators (list (fn [] (lrand-int 10))
                          'in1
                          'integer_div
                          'integer_mult
                          'integer_add
                          'integer_sub)
   :parent-selection :tournament
   :genetic-operator-probabilities {:alternation 0.5
                                    :uniform-mutation 0.5}
   :max-generations 1
   :meta-error-categories [:reuse :repetition]
   })

;(pushgp argmap)

;;;;;;;;;;;;
;; The "odd" problem: take a positive integer input and push a Boolean indicating
;; whether or not the input is an odd number. There are many ways to compute this
;; and PushGP sometimes finds unusual methods.

;; (def argmap
;;  {:use-single-thread true
;;   :error-function (fn [individual]
;;                     (assoc individual
;;                            :errors
;;                            (doall
;;                             (for [input (range 10)]
;;                               (let [state (run-push (:program individual)
;;                                                     (push-item input :input
;;                                                                (push-item input :integer
;;                                                                           (make-push-state))))
;;                                     top-bool (top-item :boolean state)]
;;                                 (if (not (= top-bool :no-stack-item))
;;                                   (if (= top-bool (odd? input)) 0 1)
;;                                   1000))))))
;;   :atom-generators (concat (registered-nonrandom)
;;                            (list (fn [] (lrand-int 100))
;;                                  'in1))
;;   :parent-selection :tournament
;;   :genetic-operator-probabilities {:alternation 0.5
;;                                     :uniform-mutation 0.5}
;;   })

;(pushgp argmap)

;;;;;;;;;;;;
;; Here is the odd problem again, except this time only using instructions from
;; specific stacks -- namely the integer, boolean, code, and exec stacks. This
;; method is useful if you want to use instructions from some stacks but not others.

;; (def argmap
;;  {:use-single-thread true
;;   :error-function (fn [individual]
;;                     (assoc individual
;;                            :errors
;;                            (doall
;;                             (for [input (range 10)]
;;                               (let [state (run-push (:program individual)
;;                                                     (push-item input :input
;;                                                                (push-item input :integer
;;                                                                           (make-push-state))))
;;                                     top-bool (top-item :boolean state)]
;;                                 (if (not (= top-bool :no-stack-item))
;;                                   (if (= top-bool (odd? input)) 0 1)
;;                                   1000))))))
;;   :atom-generators (concat (registered-for-stacks [:integer :boolean :code :exec])
;;                            (list (fn [] (lrand-int 100))
;;                                  'in1))
;;   :parent-selection :tournament
;;   :genetic-operator-probabilities {:alternation 0.5
;;                                    :uniform-mutation 0.5}
;;   })

;(pushgp argmap)

