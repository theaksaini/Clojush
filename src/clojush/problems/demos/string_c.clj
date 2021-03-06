(ns clojush.problems.demos.string-c
  (:use [clojush.pushgp.pushgp]
        [clojush pushstate interpreter random util globals]
        [clojush.pushstate]
        [clojush.interpreter]
        [clojush.random]
        clojush.instructions.tag
        [clojure.math.numeric-tower]))

; Problem: Take the input string, remove the last 2 characters, and then concat this result with itself.
; The fitness will be the number of non-matching characters in the resulting string. For example,
; desired result of "abcde" would be "abcabc", and a string of "abcabcrrr" would have an error of 3, for
; 3 too many characters, and the string "aaaaaa" would have error of 4, since it gets 2 of the characters right.

(defn string-difference
  "Returns the difference in the strings, based on character position."
  [s1 s2]
  (+ (reduce + (map #(if (= %1 %2)
                       0
                       1)
                    s1
                    s2))
     (abs (- (count s1) (count s2)))))

(defn string-remove-char
  "Returns s with first instrance of c removed, if c is in s. Otherwise, returns nil"
  [s c]
  (if (empty? s)
    nil
    (if (= (first s) c)
      (.substring s 1 (count s))
      (let [remain (string-remove-char (.substring s 1 (count s)) c)]
        (if (nil? remain)
          nil
          (str (first s) remain))))))

(defn string-char-counts-difference
  "Returns the summed length of the strings, minus 2 for each character that the strings have in common.
   For example, string-char-counts-difference of 'abcd' and 'pabja' would be 5."
  [s1 s2]
  (if (empty? s1)
    (count s2)
    (let [first-char (first s1)
          remove-from-s2 (string-remove-char s2 first-char)]
      (if (nil? remove-from-s2)
        (inc (string-char-counts-difference (.substring s1 1 (count s1)) s2))
        (string-char-counts-difference (.substring s1 1 (count s1)) remove-from-s2)))))
      

; This is a solution to the string GP problem
#_(println (run-push '(string_dup string_length 2 integer_sub string_take string_dup string_concat)
                     (push-item "abcde" :string (make-push-state))))

(defn custom-report
  "Custom generational report."
  [best population generation error-function report-simplifications]
  (let [best-test-errors (:errors (error-function best))
        best-total-test-error (apply +' best-test-errors)]
    (println ";;******************************")
    (println ";;Automatic tags used to intialize the tagspace:")
    (println ";;Tagspce-Utilization of whole population: " (doall (for [ind population]
                                                                    (intial-tagspace-utilization (str (:program ind)) (tagspace-initialization (str (:program ind)) 100 (make-push-state))))))
    (println ";;Total Error of whole Population: " (doall (for [ind population]
                                                           (apply +' (:errors ind)))))
    (println "Auto-tags:" (keys (get (tagspace-initialization (str (:program best)) 100 (make-push-state)) :tag)))
    (printf ";; -*- Digits problem report - generation %s\n" generation)(flush)
    )) 



; Define the arguments
(def argmap
 {:error-function (fn [individual]
                    (assoc individual
                           :errors
                           (doall
                            (for [input '("abcde"
                                          ""
                                          "E"
                                          "Hi"
                                          "Tom"
                                          "leprechaun"
                                          "zoomzoomzoom"
                                          "qwertyuiopasd"
                                          "GallopTrotCanter"
                                          "Quinoa")]
                              (let [final-state (run-push (:program individual)
                                                          (push-item input :input 
                                                                     (push-item input :string 
                                                                                (tagspace-initialization (str (:program individual)) 100 (make-push-state)))))
                                    top-string (top-item :string final-state)
                                    desired-output (let [short (.substring input 0 (max (- (count input) 2) 0))]
                                                     (str short short))]
                                (if (not (string? top-string))
                                  1000
                                  (+ (string-difference top-string desired-output)
                                     (string-char-counts-difference top-string desired-output))))))))
  :atom-generators (list 'in1
                         'string_length
                         'string_take
                         'string_concat
                         'string_stackdepth
                         'string_swap
                         'string_dup
                         'integer_add
                         'integer_sub
                         'integer_dup
                         'integer_swap
                         'integer_stackdepth
                         (tag-instruction-erc [:integer :boolean :string :char :exec] 1000)
                         (tagged-instruction-erc 1000)
                         (fn [] (lrand-int 10))
                         (fn [] (apply str (repeatedly (+ 1 (lrand-int 9))
                                                       #(lrand-nth (str "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                                                        "abcdefghijklmnopqrstuvwxyz"
                                                                        "0123456789"))))))
  
  :population-size 500
  :max-generations 200
  :epigenetic-markers []
  :problem-specific-report custom-report
  :report-simplifications 0
  :genetic-operator-probabilities {:alternation 0.5
                                   :uniform-mutation 0.5}
  :uniform-mutation-constant-tweak-rate 0.8
  })
