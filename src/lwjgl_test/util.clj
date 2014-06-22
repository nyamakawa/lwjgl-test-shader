(ns lwjgl-test.util)

(import '[java.nio ByteBuffer])

(defn prepare-string [strdata]
  (let [strlen (count strdata)
        version 66
        buflen (+ 1 4 (count strdata))
        bb (ByteBuffer/allocate buflen)
        buf (byte-array buflen)]
    (doto bb
      (.put (.byteValue version))
      (.putInt (.intValue strlen))
      (.put (.getBytes strdata))
      (.flip)         ;; Prepare bb for reading
      (.get buf))
    buf))

(defn hoge [] "hoge")
