(ns cahiers-notes.views.gui-utils 
  (:import
   [java.awt GraphicsEnvironment]))

(defn calculate-frame-location
  "Calculate absolute x, y coordinates to place the frame approximately.
   Return a map: {:x integer :y integer}
   "
  [frame-width frame-height]
  (let [gd (.getDefaultScreenDevice (GraphicsEnvironment/getLocalGraphicsEnvironment))
        width (.getWidth (.getDisplayMode gd))
        height (.getHeight (.getDisplayMode gd))
        x (/ (- width frame-width) 2)
        y (/ (- height frame-height) 2)] 
    (println "overall w and h" width height)
    {:x x :y y}))