(ns lwjgl-test.util)

(import '[java.nio ByteBuffer FloatBuffer])

(defn pack-string-to-bytes [strdata]
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

(defn pack-float-array [numseq]
  (let [version 66
        buflen (count numseq)
        ff (FloatBuffer/allocate buflen)
        buf (float-array buflen)]
    (doto ff
      (.put (float-array numseq))
      (.rewind))
    ff))

