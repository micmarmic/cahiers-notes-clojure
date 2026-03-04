(ns cahiers-notes.views.main-view
  (:require
   [cahiers-notes.data :as data]
   [cahiers-notes.views.gui-utils :as utils])
  (:import
   [java.awt
    BorderLayout
    Color
    Dimension
    FlowLayout]
   [java.awt.event ActionListener]
   [javax.swing
    BorderFactory
    BoxLayout
    DefaultListModel
    JButton
    JFrame
    JLabel
    JList
    JMenu
    JMenuBar
    JMenuItem
    JPanel
    JScrollPane
    JSeparator
    JTextPane
    ListCellRenderer
    SwingConstants]
   [javax.swing.border EmptyBorder]
   [javax.swing.event ListSelectionListener]))

(def TITLE "Cahiers")
(def FRAME-WIDTH 1000)
(def FRAME-HEIGHT 800)
(def CAHIER_WIDTH 240)
(def PAGES_WIDTH 240)
(def FRAME_BG_COLOR (Color. 206 187 66))

(def gui (atom {:doc-panel nil :notes-panel nil}))

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
    (getListCellRendererComponent [_ list value index selected? focused?]
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

(defn close-app
  [frame]
  (println "Closing...")
  (.dispose frame)
  (System/exit 0))

(defn add-menus
  [frame]
  (let [menu-bar (JMenuBar.)
        file-menu (JMenu. "File")
        file-exit (JMenuItem. "Exit")
        separator (JSeparator.)]
    (.setJMenuBar frame menu-bar)
    (.add menu-bar file-menu)
    (doto file-menu
      (.add  separator)
      (.add  file-exit))
    (add-action-listener file-exit #(close-app frame))))


(defn fill-docs-panel [docs-panel pagelist]
  (.removeAll docs-panel)
  (let [selected-page (.getSelectedValue pagelist)
        label (JLabel. (if (nil? selected-page) "Aucune page sélectionnée" (str "PAGE: " (:title selected-page))))
        textPane (JTextPane.)
        contents (if (nil? selected-page) "" (:content selected-page))]
    (println "docspanel" selected-page)

    (doto textPane
      (.setText contents)
      (.setEditable false))
    
    (doto docs-panel
      (.setLayout (BorderLayout.))
      (.add textPane BorderLayout/CENTER)
      (.revalidate)
      (.repaint))))

(defn fill-pages-panel [pages-panel docs-panel listbox]
  (.removeAll pages-panel)
  (let [selected-book (.getSelectedValue listbox)
        pages (data/pages-for-book-title selected-book)
        model (DefaultListModel.)
        pagelist (JList. model)
        label (JLabel. (if (nil? selected-book) "NO SELECTED BOOK" (str "Books for " selected-book)))]

    (.setCellRenderer pagelist (title-list-renderer))

    (doseq [page pages]
      (.addElement model page))
    (add-listbox-listener
     pagelist
     #(fill-docs-panel docs-panel pagelist))

    (fill-docs-panel docs-panel pagelist)

    (doto pages-panel
      (.add label)
      (.add (JScrollPane. pagelist))
      (.revalidate)
      (.repaint))))

(defn fill-cahiers-panel [cahiers-panel pages-panel docs-panel]
  (.removeAll cahiers-panel)
  (let [label (JLabel. (utils/timestamp))
        button (JButton. "Click")
        book-names (data/book-titles)
        model (DefaultListModel.)
        booklist (JList. model)]
    (println (class (.getModel booklist)))
    (doseq [name book-names]
      (.addElement model name))
    (add-listbox-listener
     booklist
     #(fill-pages-panel pages-panel docs-panel booklist))

    (. button addActionListener
       (reify ActionListener
         (actionPerformed [_ _]
           (fill-cahiers-panel cahiers-panel pages-panel docs-panel))))

    (fill-pages-panel pages-panel docs-panel booklist)
    (doto cahiers-panel
      (.add label)
      (.add (JScrollPane. booklist))
      (.revalidate)
      (.repaint))))



  ;(.add cahiers-frame (JLabel. timestamp))

(defn create-and-show-gui
  "Build and display the view"
  []
  (let [frame (JFrame. TITLE)
        main-panel (JPanel.)
        left-container (JPanel.)
        cahiers-panel (JPanel.)
        pages-panel (JPanel.)
        docs-panel (JPanel.)
        location (utils/calculate-frame-location FRAME-WIDTH FRAME-HEIGHT)]

    (reset! gui {:cahiers-panel cahiers-panel :pages-panel pages-panel})

    (doto main-panel
      (.setBackground FRAME_BG_COLOR)
      (.setBorder (EmptyBorder. 10 10 10 10))
      (.setLayout (BorderLayout.)))
    (.add frame main-panel)

    (.add main-panel left-container BorderLayout/WEST)
    (.setLayout left-container (BorderLayout.))

    (doto cahiers-panel
      (.setLayout (BoxLayout. cahiers-panel BoxLayout/PAGE_AXIS));)
      (.setBackground Color/YELLOW)
      (.setPreferredSize (Dimension. CAHIER_WIDTH (.height (.getPreferredSize cahiers-panel))))
      (.setBorder (BorderFactory/createLineBorder Color/BLACK)))
      ;(.add (JLabel. "CAHIERS"))
      ;(.add button-cahier))    
    (.add left-container cahiers-panel BorderLayout/WEST)


    (doto pages-panel
      (.setLayout (BoxLayout. pages-panel BoxLayout/PAGE_AXIS));)
      (.setBackground Color/LIGHT_GRAY)
      (.setPreferredSize (Dimension. PAGES_WIDTH (.height (.getPreferredSize pages-panel))))
      (.setBorder (BorderFactory/createLineBorder Color/BLACK))
      (.add (JLabel. "PAGES")))
    (.add left-container pages-panel BorderLayout/EAST)

    (doto docs-panel
      (.setBackground Color/ORANGE)
      (.add (JLabel. "DOCS")))
    (.add main-panel docs-panel FlowLayout/CENTER)

    
    (fill-cahiers-panel cahiers-panel pages-panel docs-panel)


    (add-menus frame)

    (doto frame
      (.setBackground FRAME_BG_COLOR)
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.setResizable true)
      (.setLocation (:x location) (:y location))
      (.setSize FRAME-WIDTH FRAME-HEIGHT)
      (.setVisible true))))
