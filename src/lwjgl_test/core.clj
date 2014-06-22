(ns lwjgl-test.core)

(require '[lwjgl-test.util :as util])

(import '[org.lwjgl.opengl DisplayMode Display GL11 GL20])

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
    gl_FragColor = vec4 ( 1.0, 1.0, 0.0, 1.0 );
  }
")

(defn put-error [log]
  (println log)
  )

;; shader-type: glEnum
;; shader-src:  shader string
(defn load-shader [shader-type shader-src]
  (def shader (GL20/glCreateShader shader-type))
  ;; convert string to java.nio.ByteBuffer
;;  (def shader-bytes (util/prepare-string shader-src))
  (println "shader: " shader)
  (println "shader-src: " shader-src)

  (GL20/glShaderSource shader shader-src)
  (GL20/glCompileShader shader)

  ;; check compile status
  (def compiled?
    (= 1 (GL20/glGetShaderi shader GL20/GL_COMPILE_STATUS)))
  (println "compiled? :" compiled?)

  (if compiled?
    ;;return shader
    shader

    ;; errored
    (let [log-length (GL20/glGetShaderi shader GL20/GL_INFO_LOG_LENGTH)]
      (let [error-log (GL20/glGetShaderInfoLog shader log-length)]
        (put-error error-log)
        (GL20/glDeleteShader shader)
        nil
        )
      )
  )
  )

(defn draw []
  (GL11/glClear GL11/GL_COLOR_BUFFER_BIT)
  (GL11/glBegin GL11/GL_LINES)


  (GL11/glEnd)
  (GL11/glFlush))

(defn start []
  (Display/setDisplayMode display-mode)
  (Display/setTitle "Hello GL")
  (Display/create)

  (def vertex-shader (load-shader GL20/GL_VERTEX_SHADER vertex-shader-src))
  (def pixel-shader (load-shader GL20/GL_FRAGMENT_SHADER pixel-shader-src))
  (if (and vertex-shader pixel-shader)
    (while (not (Display/isCloseRequested))
      (draw)
      (Display/update))
    (Display/destroy))
  )


(defn -main [& args]
  (println "Starting...")
  (start)
  )
