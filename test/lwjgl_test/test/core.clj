(ns lwjgl-test.test.core
  (:require [lwjgl-test.util :as util]
            [lwjgl-test.loader :as loader])
  (:use clojure.test))

(defn triangle-mesh []
  (let [triangle (util/gen-triangle)
        model (util/mesh-with-vertices triangle)]
    model))

(defn cube-indexed-mesh []
  (let [mesh (util/read-obj-file "cube.obj")
        model (util/generate-mesh-buffers mesh)]
    model))

(deftest sumtest []
  (is (= 2 (+ 1 1))))

(deftest triangle-test []
  (let [mesh (triangle-mesh)]
    (is (= false (util/mesh-indexed? mesh)))))

(deftest cube-test []
  (let [mesh (cube-indexed-mesh)]
    (println "indicies" (mesh :indices))
    (util/print-mesh mesh)
    (is (= true (util/mesh-indexed? mesh)))))
