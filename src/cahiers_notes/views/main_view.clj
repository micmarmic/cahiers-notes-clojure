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
  (println "Application cahiers fermée.")
  ;; TODO check for unsaved edits?
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

(defn update-docs-panel [docs-pane pagelist edit-checkbox]
  (let [selected-page (.getSelectedValue pagelist)
        contents (if (not= selected-page nil)
                   (controller/file-contents (:path selected-page))
                   "")
        edit? (.isSelected edit-checkbox)]

    (.setEditable docs-pane edit?)
    (md/reset docs-pane)
    (if edit?
      (do
        (md/clear-all-styles docs-pane)
        (.setText docs-pane contents))
      (md/add-text docs-pane contents))))

(defn update-pages [booklist pagelist docs-pane  edit-checkbox]
  (let [selected-book (.getSelectedValue booklist)
        pages (data/pages-for-book-title selected-book)
        pages-model (.getModel pagelist)]

    (.removeAllElements pages-model)
    (doseq [page pages]
      (.addElement pages-model page))
    (update-docs-panel docs-pane pagelist edit-checkbox)))

(defn update-cahiers
  "Update the list after a book was renamed.
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
       (.setSelectedValue booklist title true)))))

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
        pages-panel (JPanel.)
        pages-button-panel (JPanel.)
        add-page-button (JButton. "Ajouter")
        rename-page-button (JButton. "Renommer")
        edit-panel (JPanel.)
        edit-checkbox (JCheckBox.)
        docs-panel (JPanel.)
        docs-pane (JTextPane.)
        docs-scroll (JScrollPane. docs-pane)
        location (utils/calculate-frame-location FRAME-WIDTH FRAME-HEIGHT)
        bookmodel (DefaultListModel.)
        booklist (JList. bookmodel)
        pagemodel (DefaultListModel.)
        pagelist (JList. pagemodel)]

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
      (.add rename-cahier-button))

    (.setCellRenderer pagelist (utils/title-list-renderer))

    (doto pages-panel
      (.setLayout (BorderLayout.))
      (.setBackground Color/LIGHT_GRAY)
      (.setPreferredSize (Dimension. PAGES_WIDTH (.height (.getPreferredSize pages-panel))))
      (.setBorder (BorderFactory/createLineBorder Color/BLACK))
      (.add (JScrollPane. pagelist) BorderLayout/CENTER)
      (.add pages-button-panel BorderLayout/SOUTH))

    (.add left-container pages-panel BorderLayout/EAST)


    (doto pages-button-panel
      (.setLayout (FlowLayout.))
      (.add add-page-button)
      (.add rename-page-button))

    (doto edit-panel
      (.add (JLabel. "Éditer?"))
      (.add edit-checkbox))

    (doto docs-scroll
      (.setVerticalScrollBarPolicy JScrollPane/VERTICAL_SCROLLBAR_ALWAYS)
      (.setHorizontalScrollBarPolicy JScrollPane/HORIZONTAL_SCROLLBAR_NEVER))

    ;; (doto docs-pane
    ;;   (.setLineWrap true)
    ;;   (.setWrapStyleWord true))

    (doto docs-panel
      (.setLayout (BorderLayout.))
      (.add edit-panel BorderLayout/NORTH)
      (.add docs-scroll BorderLayout/CENTER)
      ;(.add docs-pane BorderLayout/CENTER)
      )

    ;; restrain the width of the docs-pane
    (.setPreferredSize docs-pane (Dimension. 100 100))
    (.setSize docs-pane (Dimension. 100 100))

    (.add main-panel docs-panel FlowLayout/CENTER)


    ;; callbacks
    (utils/add-listbox-listener
     booklist
     #(update-pages booklist pagelist docs-pane  edit-checkbox))

    (utils/add-action-listener
     add-cahier-button
     #(controller/add-cahier booklist))

    (utils/add-action-listener
     rename-cahier-button
     #(controller/rename-cahier booklist update-cahiers))

    (utils/add-action-listener
     add-page-button
     #(controller/add-page booklist pagelist docs-pane update-pages))

    (utils/add-action-listener
     rename-page-button
     #(controller/rename-page pagelist update-pages))

    (utils/add-action-listener
     edit-checkbox
     ; simulate a change to the page selection to redisplay the page
     #(update-docs-panel docs-pane pagelist edit-checkbox))

    (utils/add-listbox-listener
     pagelist
     #(update-docs-panel docs-pane pagelist edit-checkbox))

    ;; (.addActionListener
    ;;  edit-checkbox
    ;;  (reify ActionListener
    ;;    (actionPerformed [_ _]
    ;;      (fill-cahiers-panel cahiers-panel pages-panel docs-pane edit-checkbox))))

    ;; initial update
    (update-cahiers booklist)

    ;; finish frame configuratin and display it
    (add-menus frame)
    (doto frame
      (.setBackground FRAME_BG_COLOR)
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.setResizable true)
      (.setLocation (:x location) (:y location))
      (.setSize FRAME-WIDTH FRAME-HEIGHT)
      (.setVisible true))))
