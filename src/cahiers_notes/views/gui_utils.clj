(ns cahiers-notes.views.gui-utils 
  (:import
   [javax.swing JOptionPane]
   [java.awt GraphicsEnvironment]
   [java.awt.event ActionListener]
   [javax.swing ListCellRenderer]
   [javax.swing.event ListSelectionListener]))


(defn show-error 
  "Display the message dialog."
  [error-message]
  (JOptionPane/showMessageDialog nil error-message "Oops!" JOptionPane/ERROR_MESSAGE))

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

(defn title-for-item
  "Given a map (or any object), return the string that should appear in the list.
   If the item is a map with a `:name` key we use that, otherwise we fall back
   to `str` so the renderer never crashes."
  [item]
  (if (map? item)
    (or (:title item) (str item))
    (str item)))

(defn title-list-renderer
  "Creates a ListCellRenderer that shows only the `:name` value of each map."
  []
  (reify ListCellRenderer
    (getListCellRendererComponent [_ list value _index selected? _focused?]
      ;; Use a plain JLabel (the default renderer) but replace its text.
      (let [label (javax.swing.DefaultListCellRenderer.)]
        (.setText label (title-for-item value))
        ;; Preserve the usual selection/background handling
        (when selected?
          (.setBackground label (.getSelectionBackground list))
          (.setForeground label (.getSelectionForeground list)))
        (when (not selected?)
          (.setBackground label (.getBackground list))
          (.setForeground label (.getForeground list)))
        (.setEnabled label (.isEnabled list))
        (.setOpaque label true)
        label))))

(defn add-listbox-listener [listbox callback]
  (.addListSelectionListener
   listbox
   (reify ListSelectionListener
     (valueChanged
       [_ _]
       (callback)))))

;; TODO: this may be a generic action listener suitable for all widgets ...
(defn add-action-listener
  [widget callback]
  (.addActionListener widget (reify ActionListener (actionPerformed [_ _] (callback)))))