(ns cahiers-notes.views.main-view
  (:require
   [cahiers-notes.data :as data]
   [cahiers-notes.views.gui-utils :as utils]
   [markdown-viewer.lib.markdown-panel :as md])
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
    JCheckBox
    JFrame
    JLabel
    JList
    JMenu
    JMenuBar
    JMenuItem
    JPanel
    JScrollPane
    JSeparator
    JTextPane]
   [javax.swing.border EmptyBorder]))

(def TITLE "Cahiers")
(def FRAME-WIDTH 1000)
(def FRAME-HEIGHT 800)
(def CAHIER_WIDTH 240)
(def PAGES_WIDTH 240)
(def FRAME_BG_COLOR (Color. 206 187 66))

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
    (utils/add-action-listener file-exit #(close-app frame))))


(defn fill-docs-panel [docs-pane pagelist edit-checkbox]
  (let [selected-page (.getSelectedValue pagelist)
        ;textPane (JTextPane.)
        contents (if (nil? selected-page) "" (:content selected-page))
        edit? (.isSelected edit-checkbox)]

    (.setEditable docs-pane edit?)
    (md/reset docs-pane)

    (if edit?
      (.setText docs-pane contents)
      (md/add-text docs-pane contents))))
    

(defn fill-pages-panel [pages-panel listbox md-pane edit-checkbox]
  (.removeAll pages-panel)
  (let [selected-book (.getSelectedValue listbox)
        pages (data/pages-for-book-title selected-book)
        model (DefaultListModel.)
        pagelist (JList. model)
        label (JLabel. (if (nil? selected-book) "NO SELECTED BOOK" (str "Books for " selected-book)))]

    (.setCellRenderer pagelist (utils/title-list-renderer))

    (doseq [page pages]
      (.addElement model page))
    (utils/add-listbox-listener
     pagelist
     #(fill-docs-panel md-pane pagelist edit-checkbox))
    
    (fill-docs-panel md-pane pagelist edit-checkbox)

    (doto pages-panel
      (.add label)
      (.add (JScrollPane. pagelist))
      (.revalidate)
      (.repaint))))

(defn fill-cahiers-panel [cahiers-panel pages-panel docs-pane edit-checkbox]
  (.removeAll cahiers-panel)
  (let [label (JLabel. (utils/timestamp))
        book-names (data/book-titles)
        model (DefaultListModel.)
        booklist (JList. model)]
    (println (class (.getModel booklist)))
    (doseq [name book-names]
      (.addElement model name))
    (utils/add-listbox-listener
     booklist
     #(fill-pages-panel pages-panel booklist docs-pane  edit-checkbox))

    (fill-pages-panel pages-panel booklist docs-pane edit-checkbox)
    (doto cahiers-panel
      (.add label)
      (.add (JScrollPane. booklist))
      (.revalidate)
      (.repaint))))

(defn create-and-show-gui
  "Build and display the view"
  []
  (let [frame (JFrame. TITLE)
        main-panel (JPanel.)
        left-container (JPanel.)
        cahiers-panel (JPanel.)
        pages-panel (JPanel.)
        edit-panel (JPanel.)
        edit-checkbox (JCheckBox.)
        docs-panel (JPanel.)
        docs-pane (JTextPane.)
        location (utils/calculate-frame-location FRAME-WIDTH FRAME-HEIGHT)]

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

    (doto edit-panel
      (.add (JLabel. "Éditer?"))
      (.add edit-checkbox))

    ;; (.addActionListener
    ;;  edit-checkbox
    ;;  (reify ActionListener
    ;;    (actionPerformed [_ _]
    ;;      (fill-cahiers-panel cahiers-panel pages-panel docs-pane edit-checkbox))))



    (doto docs-panel
      (.setLayout (BorderLayout.))
      (.add edit-panel BorderLayout/NORTH)
      (.add docs-pane BorderLayout/CENTER))
    (.add main-panel docs-panel FlowLayout/CENTER)

    (fill-cahiers-panel cahiers-panel pages-panel docs-pane edit-checkbox)

    (add-menus frame)

    (doto frame
      (.setBackground FRAME_BG_COLOR)
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.setResizable true)
      (.setLocation (:x location) (:y location))
      (.setSize FRAME-WIDTH FRAME-HEIGHT)
      (.setVisible true))))
