(ns lwjgl-test.util
    (:import [org.lwjgl BufferUtils]))

;; convert float-array to float buffer
(defn to-float-buffer [array-of-float]
    (let [vertices-buffer (BufferUtils/createFloatBuffer (count array-of-float))]
    (do
      (doto vertices-buffer
        (.put (float-array array-of-float))
        (.flip))
      vertices-buffer)))

;; generate triangle
(defn gen-triangle []
  [0.0 0.5 0.0
   -0.5 -0.5 0.0
   0.5 -0.5 0.0])

;; model schema
(defstruct polygon-model :vertices :indices :vertex-buffer :index-buffer :polygon-count)

(defn gen-polygon [vertices]
  (struct-map  polygon-model
          :vertices vertices
          :indices nil
          :vertex-buffer (to-float-buffer vertices)
          :index-buffer nil
          :polygon-count (/ (count vertices) 3)))






