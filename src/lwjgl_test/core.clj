(ns lwjgl-test.core
  (:require [lwjgl-test.util :as util])
  (:import [org.lwjgl.opengl DisplayMode Display GL11 GL15 GL20 Util]
            [org.lwjgl BufferUtils]))
;;(require '[org.lwjgl.opengl.util :as lwjgl-util])

(def width 640)
(def height 480)
(def display-mode (DisplayMode. width height))

(def vertex-shader-src
  "
  attribute vec4 vPosition;
  void main()
  {
  gl_Position = vPosition;
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

(defn shader-error [shader]
     (let [log-length (GL20/glGetShaderi shader GL20/GL_INFO_LOG_LENGTH)
           error-log (GL20/glGetShaderInfoLog shader log-length)]
        (put-error error-log)
        (GL20/glDeleteShader shader)
        nil))

;; shader-type: glEnum
;; shader-src:  shader string
(defn load-shader [shader-type shader-src]
  (let [shader (GL20/glCreateShader shader-type)]
  ;; convert string to java.nio.ByteBuffer
  ;;  (def shader-bytes (util/prepare-string shader-src))
    (do
      (println "shader: " shader)
      (println "shader-src: " shader-src)

      (GL20/glShaderSource shader shader-src)
      (GL20/glCompileShader shader)

  ;; check compile status
      (let [compiled? (= GL11/GL_TRUE (GL20/glGetShaderi shader GL20/GL_COMPILE_STATUS))]
        (do
          (println "compiled? :" compiled?)
          (if compiled?
            shader
            (shader-error shader)))))))

(defn check-error []
  (let [gl-error (GL11/glGetError)]
    (if (not (= GL11/GL_NO_ERROR))
      (println "error: " (Util/translateGLErrorString gl-error))
    )))

(defn vertices-bytebuffer []
  (let  [vertices [0.0 0.5 0.0
                   -0.5 -0.5 0.0
                   0.5 -0.5 0.0]
         vertices-buffer (BufferUtils/createFloatBuffer (count vertices))]
    (do
      (doto vertices-buffer
        (.put (float-array vertices))
        (.flip))
      vertices-buffer)))

(defn draw [program]
  (let [vertices-buffer (vertices-bytebuffer)]
    (do
      (GL11/glViewport 0 0 width height)
      (GL11/glClear GL11/GL_COLOR_BUFFER_BIT)

      (GL20/glUseProgram program)
      ;;(GL20/glVertexAttribPointer 0 3 false 0 vertices-buffer)
      (let [buf-id (GL15/glGenBuffers)]
        ;;    (println "buf-id: " buf-id)
        (do
          (check-error)
          (GL15/glBindBuffer GL15/GL_ARRAY_BUFFER buf-id)
          (check-error)
          (GL15/glBufferData GL15/GL_ARRAY_BUFFER vertices-buffer GL15/GL_STATIC_DRAW)
          (check-error)))))

;;  (GL20/glEnableVertexAttribArray 0)
  (GL11/glEnableClientState GL11/GL_VERTEX_ARRAY)
  (GL11/glVertexPointer 3 GL11/GL_FLOAT 0 0)
  (GL11/glDrawArrays GL11/GL_TRIANGLES 0 3)
  (Util/checkGLError))

(defn shader-program-error [program]
  (let [log-length (GL20/glGetProgrami program GL20/GL_INFO_LOG_LENGTH)
        error-log (GL20/glGetProgramInfoLog program log-length)]
      (put-error error-log)
      (GL20/glDeleteProgram program)
      nil))

(defn draw-loop-with-program [program]
  (GL11/glClearColor 0.0 0.0 0.0 0.0)

  (println "trace 1")
  (while (not (Display/isCloseRequested))

    (draw program)
    (Display/update))

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
            (draw-loop-with-program program)
            (shader-program-error program)
            ))))))

(defn start []
  (Display/setDisplayMode display-mode)
  (Display/setTitle "Hello GL")
  (Display/create)

  (let [vertex-shader (load-shader GL20/GL_VERTEX_SHADER vertex-shader-src)
        pixel-shader (load-shader GL20/GL_FRAGMENT_SHADER pixel-shader-src)]
      (do
        (println "vertex-shader: " vertex-shader)
        (println "pixel-shader: " pixel-shader)

        (if (and vertex-shader pixel-shader)
          (start-draw-loop vertex-shader pixel-shader)))))

(defn -main [& args]
  (println "Starting...")
  (start))
