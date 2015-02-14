(ns lwjgl-test.util
    (:import [org.lwjgl BufferUtils])
    (:require [clojure.string :as str]))

;; convert float-array to float buffer
(defn to-float-buffer [array-of-float]
    (let [vertices-buffer (BufferUtils/createFloatBuffer (count array-of-float))]
    (do
      (doto vertices-buffer
        (.put (float-array array-of-float))
        (.flip))
      vertices-buffer)))

(defn to-int-buffer [array-of-int]
    (let [int-buffer (BufferUtils/createIntBuffer (count array-of-int))]
    (do
      (doto int-buffer
        (.put (int-array array-of-int))
        (.flip))
      int-buffer)))

;; generate triangle
(defn gen-triangle []
  [0.0 0.366 0.0
   -0.5 -0.5 0.0
   0.5 -0.5 0.0])
;;
;; def mesh struct and utilities
;;
(defstruct polygon-mesh :vertices :indices :vertex-buffer :index-buffer :polygon-count)

(defn mesh-indexed? [mesh]
  (not (or (= (mesh :indices) [])
    (= (mesh :indices) nil))))

(defn count-indices [mesh]
  (count (:indices mesh)))

(defn count-vertices [mesh]
  (count (:vertices mesh)))

(defn count-triangles [mesh]
  (if (mesh-indexed? mesh)
    (/ (count-indices mesh) 3)
    (/ (count-vertices mesh) 3)))

(defn mesh-with-vertices [vertices]
  (struct-map polygon-mesh
    :vertices vertices
    :vertex-buffer (to-float-buffer vertices)
    :polygon-count (/ (count vertices) 3)))

(defn generate-mesh []
  (struct-map polygon-mesh
    :vertices []
    :indices []))

(defn generate-mesh-buffers [mesh]
  (assoc mesh
    :vertex-buffer (to-float-buffer (:vertices mesh))
    :index-buffer (to-int-buffer (:indices mesh))
    :polygon-count (count-triangles mesh)))

(defn print-mesh [mesh]
  (print mesh))

;;
;; wavefront obj file parser
;;
(defn parse-floats [vars]
  (mapv #(Float/parseFloat %) vars))

(defn parse-integers [vars]
  (mapv #(Integer/parseInt %) vars))

(defn parse-vertices [mesh vars]
  (assoc mesh :vertices
    (concat (:vertices mesh) (parse-floats vars))))

(defn parse-indices [mesh vars]
;  (println "parse-indices:" vars)
  (assoc mesh :indices
    (concat (:indices mesh) (parse-integers vars))))

(defn read-obj-line [line mesh]
  (let [tokens (str/split line #"\s+")]
    (let [line-type (first tokens)
          line-vars (rest tokens)]
;        (println "tokens:" tokens)
        (cond (= line-type "v")
                (parse-vertices mesh line-vars)
              (= line-type "f")
                (parse-indices mesh line-vars)
              :else mesh))))

(defn read-mesh-obj-file-rec [mesh lines]
  (let [line (first lines)]
    (if (= nil line)
      mesh
      (let [new-mesh (read-obj-line line mesh)]
          (recur new-mesh (rest lines))))))

(defn read-obj-file [filename]
  (with-open [rdr (clojure.java.io/reader filename)]
    (let [lines (line-seq rdr)]
      (read-mesh-obj-file-rec (generate-mesh) lines))))
