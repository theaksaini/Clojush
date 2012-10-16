(ns clojush.pushgp.breed
  (:use [clojush.random]
        [clojush.pushgp.parent-selection]
        [clojush.pushgp.genetic-operators]
        [clojush.simplification]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; genetic operators

(defn breed
  "Replaces the state of the given agent with an individual bred from the given population (pop), 
   using the given parameters."
  [agt location rand-gen pop error-function max-points atom-generators 
   mutation-probability mutation-max-points crossover-probability simplification-probability 
   tournament-size reproduction-simplifications trivial-geography-radius
   gaussian-mutation-probability gaussian-mutation-per-number-mutation-probability 
   gaussian-mutation-standard-deviation boolean-gsxover-probability
   boolean-gsxover-new-code-max-points deletion-mutation-probability]
  (binding [*thread-local-random-generator* rand-gen]
    (let [n (lrand)]
      (cond 
        ;; mutation
        (< n mutation-probability)
        (let [parent (select pop tournament-size trivial-geography-radius location)]
          (assoc (mutate parent mutation-max-points max-points atom-generators) :parent parent))
        ;; crossover
        (< n (+ mutation-probability crossover-probability))
        (let [first-parent (select pop tournament-size trivial-geography-radius location)
              second-parent (select pop tournament-size trivial-geography-radius location)]
          (assoc (crossover first-parent second-parent max-points) :parent first-parent))
        ;; simplification
        (< n (+ mutation-probability crossover-probability simplification-probability))
        (let [parent (select pop tournament-size trivial-geography-radius location)]
          (assoc (auto-simplify parent error-function reproduction-simplifications false 1000)
                 :parent parent))
        ;; gaussian mutation
        (< n (+ mutation-probability crossover-probability simplification-probability 
                gaussian-mutation-probability))
        (let [parent (select pop tournament-size trivial-geography-radius location)]
          (assoc (gaussian-mutate 
                   parent gaussian-mutation-per-number-mutation-probability gaussian-mutation-standard-deviation)
                 :parent parent))
        ;; boolean gsxover
        (< n (+ mutation-probability crossover-probability simplification-probability 
                gaussian-mutation-probability boolean-gsxover-probability))
        (let [first-parent (select pop tournament-size trivial-geography-radius location)
              second-parent (select pop tournament-size trivial-geography-radius location)]
          (assoc (boolean-gsxover first-parent second-parent boolean-gsxover-new-code-max-points max-points atom-generators)
                 :parent first-parent))
        ;; deletion mutation
        (< n (+ mutation-probability crossover-probability simplification-probability 
                gaussian-mutation-probability boolean-gsxover-probability deletion-mutation-probability))
        (let [parent (select pop tournament-size trivial-geography-radius location)]
          (assoc (delete-mutate parent) :parent parent))
        ;; replication
        true 
        (let [parent (select pop tournament-size trivial-geography-radius location)]
          (assoc parent :parent parent))))))
