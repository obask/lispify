(import java.nio.file.Files)

(ns reader
  (:require [clojure.string :as str]))

(defn tokenize [exp]
  (remove empty? (-> exp
                   (str/replace "(" " ( ")
                   (str/replace ")" " ) ")
                   (str/replace "'" " ' ")
                   (str/split #"\s+"))))

(declare micro-read read-list)

(defn micro-read [[t & ts]]
  (case t
    "(" (read-list '() ts)
    "'" (let [[new-t new-ts] (micro-read ts)]
          [(list "quote" new-t) new-ts])
    [t ts]))

(defn read-list [list-so-far tokens]
  (let [[t ts] (micro-read tokens)]
    (case t
      ")" [(reverse list-so-far) ts]
      "(" (let [[new-list new-tokens] (read-list '() ts)]
            (read-list (cons new-list list-so-far) new-tokens))
      (read-list (cons t list-so-far) ts))))

(defn read* [exp]
  (first (micro-read (tokenize exp))))


(def CODE_PATH "/Users/obaskakov/code.scm")


;(defn mainz []
;  (println "REPL started!")
;
;  (loop [env (env/make-env)]
;    (if-let [chars (seq (read-line))]
;      (try
;        (println (eval/eval* (reader/read* (apply str chars)) env))
;        (catch Exception ex
;          (println (.getMessage ex)))))
;    (recur env)))


(defrecord ABranch [val])

(defrecord AString [val])




(defn make_ast [tokens state]
  (if (empty? tokens)
    (let []
      [[] (ABranch. (reverse state))]
      )
    ;else
    (let [headx (first tokens)
          tailx (next tokens)]
      (case headx
        "("
        (let [abs (make_ast tailx [])]
          (make_ast (first abs)
            (cons (second abs) state)))
        ")"
        (let [res
          [tailx (ABranch. (reverse state))]]
          res)
        ; else
        (let [xxx
              (make_ast tailx
                (cons (AString. headx)
                  state))]
        xxx)
        ))))


(defn print_ast [tree]
  (cond
    (instance? ABranch tree)
    (clojure.string/join " " (concat "[" (map print_ast (:val tree)) "]"))
    (instance? AString tree)
    (:val tree)))



    (defn mainx []
  ;  (def tmp (java.io.BufferedReader. (java.io.FileReader. CODE_PATH)))

  ;  (def tmp (java.nio.file.Paths/get CODE_PATH (make-array String 0)))
  ;  (def tmp (java.nio.file.Files/lines tmp))
  ;  (def tmp (.map tmp (fn [] 42)))

  ;  (def tmp (.reareadAllLines CODE_PATH))

  (def tmp (line-seq (java.io.BufferedReader. (java.io.FileReader. CODE_PATH))))
  (def tmp (map (fn [x] (str/replace x "(" " ( ")) tmp))
  (def tmp (flatten (map (fn [x] (str/replace x ")" " ) ")) tmp)))
  (def tmp (flatten (map (fn [x] (str/split x #" ")) tmp)))
  (def tmp (flatten (map (fn [x] (str/split x #"\t")) tmp)))
  (def tmp (filter (fn [x] (not-empty x)) tmp))

;  (doseq [x tmp] (println "ololo:" x))

;  (println "Success!")

  (def res (make_ast tmp []))
  (println (print_ast (second res)))
  )


(mainx)











