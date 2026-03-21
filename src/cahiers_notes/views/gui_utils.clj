(ns cahiers-notes.views.gui-utils
  (:import
   [java.awt GraphicsEnvironment]
   [java.awt.event ActionListener WindowListener]
   [javax.swing AbstractAction JOptionPane]
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
       ; process when change is done else it is done twice
       (when (.getValueIsAdjusting (.getSelectionModel listbox))
         (callback))))))


(defn add-action-listener
  [widget callback]
  (.addActionListener widget (reify ActionListener (actionPerformed [_ _] (callback)))))

(defn add-window-listener
  [frame callback]
  (.addWindowListener
   frame
   (reify WindowListener
     (windowOpened [_ _]) ; nothing to do
     (windowActivated [_ _]) ; nothing to do
     (windowDeactivated [_ _]) ; nothing to do
     (windowClosing [_ _] (callback)))))