(ns cahiers-notes.views.main-view
  (:require
   [cahiers-notes.controller :as controller]
   [cahiers-notes.data :as data]
   [cahiers-notes.views.gui-utils :as utils]
   [markdown-viewer.lib.markdown-panel :as md])
  (:import
   [java.awt
    BorderLayout
    Color
    Dimension
    FlowLayout]
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
(def FRAME-WIDTH 1200)
(def FRAME-HEIGHT 800)
(def CAHIER_WIDTH 300)
(def PAGES_WIDTH 300)
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

(defn update-cahiers
  "Update the list after a book was rename.
   Rebuild the list and select the title if provided."
  ([booklist]
   (update-cahiers booklist ""))
  ([booklist title]
   (let [book-names (data/book-titles)
         model (.getModel booklist)]
     (.removeAllElements model)
     (doseq [name book-names]
       (.addElement model name))
     (when (not= title "")
       (.setSelectedValue booklist title true)
       ))))

(defn create-and-show-gui
  "Build and display the view"
  []
  (let [frame (JFrame. TITLE)
        main-panel (JPanel.)
        left-container (JPanel.)
        cahiers-panel (JPanel.)
        cahier-button-panel (JPanel.)
        add-cahier-button (JButton. "Ajouter")
        rename-cahier-button (JButton. "Nom")
        delete-cahier-button (JButton. "Supprimer")
        pages-panel (JPanel.)
        edit-panel (JPanel.)
        edit-checkbox (JCheckBox.)
        docs-panel (JPanel.)
        docs-pane (JTextPane.)
        location (utils/calculate-frame-location FRAME-WIDTH FRAME-HEIGHT)
        bookmodel (DefaultListModel.)
        booklist (JList. bookmodel)]

    (doto main-panel
      (.setBackground FRAME_BG_COLOR)
      (.setBorder (EmptyBorder. 10 10 10 10))
      (.setLayout (BorderLayout.)))
    (.add frame main-panel)
    (.add main-panel left-container BorderLayout/WEST)

    (doto cahiers-panel
      (.setLayout (BorderLayout.));)
      (.setBackground Color/YELLOW)
      (.setPreferredSize (Dimension. CAHIER_WIDTH (.height (.getPreferredSize cahiers-panel))))
      (.setBorder (BorderFactory/createLineBorder Color/BLACK))
      (.add (JScrollPane. booklist) BorderLayout/CENTER)
      (.add cahier-button-panel BorderLayout/PAGE_END))
      ;(.add (JLabel. "CAHIERS"))
      ;(.add button-cahier))    
    (.setLayout left-container (BorderLayout.))
    (.add left-container cahiers-panel BorderLayout/WEST)

    (.setLayout cahier-button-panel (FlowLayout.))
    (doto
     cahier-button-panel
      (.add add-cahier-button)
      (.add rename-cahier-button)
      (.add delete-cahier-button))

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

    (doto docs-panel
      (.setLayout (BorderLayout.))
      (.add edit-panel BorderLayout/NORTH)
      (.add docs-pane BorderLayout/CENTER))
    (.add main-panel docs-panel FlowLayout/CENTER)


    ;; callbacks
    (utils/add-listbox-listener
     booklist
     #(fill-pages-panel pages-panel booklist docs-pane  edit-checkbox))

    (utils/add-action-listener
     add-cahier-button
     #(controller/add-cahier booklist))
    
    (utils/add-action-listener
     rename-cahier-button
     #(controller/rename-cahier booklist update-cahiers))

    ;; (.addActionListener
    ;;  edit-checkbox
    ;;  (reify ActionListener
    ;;    (actionPerformed [_ _]
    ;;      (fill-cahiers-panel cahiers-panel pages-panel docs-pane edit-checkbox))))
    
    ;; initial update
    (update-cahiers booklist)
    (fill-pages-panel pages-panel booklist docs-pane edit-checkbox)

    ;; finish frame configuratin and display it
    (add-menus frame)
    (doto frame
      (.setBackground FRAME_BG_COLOR)
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.setResizable true)
      (.setLocation (:x location) (:y location))
      (.setSize FRAME-WIDTH FRAME-HEIGHT)
      (.setVisible true))))
