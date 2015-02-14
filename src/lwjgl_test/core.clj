(ns lwjgl-test.core
  (:require [lwjgl-test.util :as util]
            [lwjgl-test.loader :as loader])
  (:import [org.lwjgl.opengl DisplayMode Display GL11 GL15 GL20 Util]
            [org.lwjgl BufferUtils]))
;;(require '[org.lwjgl.opengl.util :as lwjgl-util])

(def width 640)
(def height 480)
(def display-mode (DisplayMode. width height))

(def vertex-shader-src
  "
  attribute vec4 vPosition;
  uniform float angle;

  float PI = 3.14159265358979323846264;
  float rad_angle = angle*PI/180.0;

  void main()
  {
  vec4 a = vPosition;
  vec4 b = a;
  b.x = a.x*cos(rad_angle) - a.y*sin(rad_angle);
  b.y = a.y*cos(rad_angle) + a.x*sin(rad_angle);
//  gl_Position = gl_ModelViewProjectionMatrix*b;
  gl_Position = b;

  //gl_Position = vPosition;
  }
  ")

(def pixel-shader-src
  "
  //precision mediump float
  void main()
  {
  gl_FragColor = vec4 ( 1.0, 0.0, 0.0, 1.0 );
  }
  ")

(defn put-error [log]
  (println log))

;; shader error handling
(defn shader-error [shader]
  (let [log-length (GL20/glGetShaderi shader GL20/GL_INFO_LOG_LENGTH)
        error-log (GL20/glGetShaderInfoLog shader log-length)]
    (put-error error-log)
    (GL20/glDeleteShader shader)
    nil))

;; load shader
;; shader-type: glEnum
;; shader-src:  shader string
(defn load-shader [shader-type shader-src]
  (let [shader (GL20/glCreateShader shader-type)]
    (println "shader: " shader)
    (println "shader-src: " shader-src)

    (GL20/glShaderSource shader shader-src)
    (GL20/glCompileShader shader)

    ;; check compile status
    (let [compiled? (= GL11/GL_TRUE (GL20/glGetShaderi shader GL20/GL_COMPILE_STATUS))]
      (println "compiled? :" compiled?)
      (if compiled?
        shader
        (shader-error shader)))))

;; gl error handling
(defn check-error []
  (let [gl-error (GL11/glGetError)]
    (if (not (= GL11/GL_NO_ERROR))
      (println "error: " (Util/translateGLErrorString gl-error)))))

; bind mesh to gl buffers;
(defn bind-vertex-buffers [mesh]
  ; bind vertices
  (let [buf-id (GL15/glGenBuffers)]
    ;;    (println "buf-id: " buf-id)
    (check-error)
    (GL15/glBindBuffer GL15/GL_ARRAY_BUFFER buf-id)
    (check-error)
    (GL15/glBufferData GL15/GL_ARRAY_BUFFER (:vertex-buffer mesh) GL15/GL_STATIC_DRAW)
    (check-error))

  ; indexed buffer
  (if (util/mesh-indexed? mesh)
    (let [buf-id (GL15/glGenBuffers)]
      ;;    (println "buf-id: " buf-id)
      (check-error)
      (GL15/glBindBuffer GL15/GL_ELEMENT_ARRAY_BUFFER buf-id)
      (check-error)
      (GL15/glBufferData GL15/GL_ELEMENT_ARRAY_BUFFER (:index-buffer mesh) GL15/GL_STATIC_DRAW)
      (check-error))))

;; drawing function (runs once in each draw loop)
(defn draw [program mesh]
;  (let [triangle (util/gen-triangle)
;        model (util/mesh-with-vertices triangle)]
    (GL11/glViewport 0 0 width height)
    (GL11/glClear GL11/GL_COLOR_BUFFER_BIT)

    (GL20/glUseProgram program)

    ;; bind vertex buffers
    (bind-vertex-buffers mesh)

    ;; draw calls
    (GL11/glEnableClientState GL11/GL_VERTEX_ARRAY)
    (GL11/glVertexPointer 3 GL11/GL_FLOAT 0 0)

    (if (util/mesh-indexed? mesh)
      (GL11/glDrawArrays GL11/GL_TRIANGLES 0 3)
      (GL11/glDrawElements GL11/GL_TRIANGLES (util/count-indices mesh) GL11/GL_UNSIGNED_SHORT 0))
    (Util/checkGLError))

;; program error
(defn shader-program-error [program]
  (let [log-length (GL20/glGetProgrami program GL20/GL_INFO_LOG_LENGTH)
        error-log (GL20/glGetProgramInfoLog program log-length)]
      (put-error error-log)
      (GL20/glDeleteProgram program)
      nil))

(def angle-step 0.4)

;; runs draw loop
(defn draw-loop-with-program [program mesh]
  (GL11/glClearColor 0.0 0.0 0.0 0.0)

  (println "(util/mesh-indexed? mesh)" (util/mesh-indexed? mesh))
  (println "trace 1")
  (loop [angle 0.0]
    (if-not (Display/isCloseRequested)
      (do
       ;; (println "angle: " angle)
        (let [angle-loc (GL20/glGetUniformLocation program "angle")]
          (GL20/glUniform1f angle-loc angle))

        (draw program mesh)
        (Display/update)
        (recur (mod (+ angle angle-step) 360.0)))))

  (Display/destroy))

(defn start-draw-loop [vertex-shader pixel-shader]
  (let [program (GL20/glCreateProgram)]
    (println "program: " program)

    (if-not (= program 0)
      (do
        (GL20/glAttachShader program vertex-shader)
        (GL20/glAttachShader program pixel-shader)

        (GL20/glLinkProgram program)

        (let [linked? (= GL11/GL_TRUE (GL20/glGetProgrami program GL20/GL_LINK_STATUS))]
          (GL20/glBindAttribLocation program 0 "vPosition")

          (if linked?
            (do
              (let [mesh (util/read-obj-file "cube.obj")
                    model (util/generate-mesh-buffers mesh)]
                (draw-loop-with-program program model)))
            (shader-program-error program)))))))

(defn start []
  (Display/setDisplayMode display-mode)
  (Display/setTitle "Hello GL")
  (Display/create)

  (println "GL_VERSION:" (GL11/glGetString GL11/GL_VERSION))

  (let [vertex-shader (load-shader GL20/GL_VERTEX_SHADER vertex-shader-src)
        pixel-shader (load-shader GL20/GL_FRAGMENT_SHADER pixel-shader-src)]
      (do
        (println "vertex-shader: " vertex-shader)
        (println "pixel-shader: " pixel-shader)

        (if (and vertex-shader pixel-shader)
          (start-draw-loop vertex-shader pixel-shader)
          (println "faild to load some shaders")))))

(defn -main [& args]
  (println "Starting...")
  (let [mesh (util/read-obj-file "cube.obj")]
    (if (= (first args) "dump")
      (util/print-mesh mesh))
    (start)))
