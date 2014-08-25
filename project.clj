(defproject lwjgl-test "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main lwjgl-test.core
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.lwjgl.lwjgl/lwjgl "2.9.1"]
                 [org.lwjgl.lwjgl/lwjgl_util "2.9.1"]
                 [org.lwjgl.lwjgl/lwjgl-platform "2.9.1"
                  :classifier "natives-osx"
                  ;; LWJGL stores natives in the root of the jar; this
                  ;; :native-prefix will extract them.
                  :native-prefix ""]
                 [net.mikera/core.matrix "0.29.1"]]
;;                  :jvm-opts [~(str "-Djava.library.path=native/:"
;;                   (System/getProperty "java.library.path"))]
                 )
