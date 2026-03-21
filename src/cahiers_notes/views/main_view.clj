(ns cahiers-notes.views.main-view
  (:require
   [cahiers-notes.controller :as controller]
   [cahiers-notes.data :as data]
   [cahiers-notes.views.gui-utils :as utils]
   [clojure.string :as str]
   [markdown-viewer.lib.markdown-panel :as md])
  (:import
   [java.awt
    BorderLayout
    Color
    Dimension
    FlowLayout]
   [javax.swing
    AbstractAction
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

(def TITLE "Cahiers de notes")
(def FRAME-WIDTH 1200)
(def FRAME-HEIGHT 800)
(def CAHIER_WIDTH 300)
(def PAGES_WIDTH 300)
(def FRAME_BG_COLOR (Color. 206 187 66))
(def BUTTON-PANEL-DIMENSION (Dimension. 100 40))

(defn add-menus
  [frame pagelist docs-pane edit-check-box]
  (let [menu-bar (JMenuBar.)
        file-menu (JMenu. "Fichiers")
        file-exit (JMenuItem. "Quitter")
        edit-menu (JMenu. "Éditer")
        edit-toggle (JMenuItem. "Changer mode édition")
        separator (JSeparator.)]
    (.setJMenuBar frame menu-bar)
    (.add menu-bar file-menu)
    (doto file-menu
      (.add  separator)
      (.add  file-exit))
    (.add menu-bar edit-menu)
    (.add edit-menu edit-toggle)


    


    (utils/add-action-listener edit-toggle #(controller/menu-toggle-edit pagelist docs-pane edit-check-box))
    (utils/add-action-listener file-exit #(controller/close-app frame pagelist docs-pane))))

(defn update-docs-panel [pagelist docs-pane edit-checkbox]
  (let [selected-page (.getSelectedValue pagelist)
        edit? (.isSelected edit-checkbox)]

    (println "-=-------------------------")
    (println "update-docs-panel" (.getText docs-pane) (.getSelectedValue pagelist))



    ; load the file AFTER the save (or not) above
    (let [contents-result (if (not= selected-page nil)
                            (controller/file-contents (:path selected-page))
                            {:success ""})
          contents (:success contents-result)
          error (:error contents-result)]
      (println "contents:" contents)
      (when error 
        (println "ERROR:" error))

      (when (not (:error contents-result))
        (md/clear-all-styles docs-pane)
        (md/clear-document docs-pane)
        (if edit?
          (.setText docs-pane contents)
          (md/add-text docs-pane contents))))))
 

(defn update-pages [booklist pagelist docs-pane edit-checkbox]
       ; manage editing-state and page content
  (let [selected-page (.getSelectedValue pagelist)]
    (when (and selected-page @controller/editing-state)
      (when (not= selected-page @controller/current-page)
        (controller/clear-edit-checkbox-and-state edit-checkbox)
        (controller/save-page (:path selected-page) (.getText docs-pane))
        (controller/save-doc-disable-edit pagelist docs-pane edit-checkbox)))
    (controller/set-current-page selected-page))
  
  (md/clear-document docs-pane)

  (let [selected-book (.getSelectedValue booklist)
        pages (sort-by #(str/lower-case (:title %)) (data/pages-for-book-title selected-book))
        pages-model (.getModel pagelist)]
    (.removeAllElements pages-model)
    (doseq [page pages]
      (.addElement pages-model page))
    (update-docs-panel pagelist docs-pane edit-checkbox)))

(defn update-cahiers
  "Update the list after a book was renamed.
   Rebuild the list and select the title if provided.
   Must also reload the pages since the path will have changed."
  ([booklist pagelist docs-pane  edit-checkbox]
   (update-cahiers booklist pagelist docs-pane  edit-checkbox ""))
  ([booklist pagelist docs-pane  edit-checkbox title]   
   (let [book-names (data/book-titles)
         model (.getModel booklist)]
     (.removeAllElements model)
     (doseq [name (sort-by #(str/lower-case %) book-names)]
       (.addElement model name))
     (when (not= title "")
       (.setSelectedValue booklist title true)
       (update-pages booklist pagelist docs-pane  edit-checkbox)
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
      (.setPreferredSize BUTTON-PANEL-DIMENSION)
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
      (.setPreferredSize BUTTON-PANEL-DIMENSION)
      (.setLayout (FlowLayout.))
      (.add add-page-button)
      (.add rename-page-button))

    (doto edit-panel
      (.setPreferredSize BUTTON-PANEL-DIMENSION)
      (.add (JLabel. "Éditer"))
      (.add edit-checkbox))
    
    (.setEditable docs-pane false)

    (doto docs-scroll
      (.setVerticalScrollBarPolicy JScrollPane/VERTICAL_SCROLLBAR_ALWAYS)
      (.setHorizontalScrollBarPolicy JScrollPane/HORIZONTAL_SCROLLBAR_NEVER))

    ;; (doto docs-pane
    ;;   (.setLineWrap true)
    ;;   (.setWrapStyleWord true))

    (doto docs-panel
      (.setBorder (BorderFactory/createLineBorder Color/BLACK))
      (.setLayout (BorderLayout.))
      (.add edit-panel BorderLayout/SOUTH)
      (.add docs-scroll BorderLayout/CENTER))
        ;(.add docs-pane BorderLayout/CENTER)


    ;; restrain the width of the docs-pane
    ;; (.setPreferredSize docs-pane (Dimension. 100 100))
    ;; (.setSize docs-pane (Dimension. 100 100))

    (.add main-panel docs-panel FlowLayout/CENTER)


    ;; callbacks
    (utils/add-window-listener
     frame
     #(controller/close-app frame pagelist docs-pane))

    (utils/add-listbox-listener
     booklist
     #(update-pages booklist pagelist docs-pane edit-checkbox))

    (utils/add-action-listener
     add-cahier-button
     #(controller/add-cahier booklist))

    (utils/add-action-listener
     rename-cahier-button
     #(controller/rename-cahier booklist pagelist docs-pane edit-checkbox update-cahiers))

    (utils/add-action-listener
     add-page-button
     #(controller/add-page booklist pagelist docs-pane update-pages))

    (utils/add-action-listener
     rename-page-button
     #(controller/rename-page pagelist update-pages))

    (utils/add-action-listener
     edit-checkbox
       ; simulate a change to the page selection to redisplay the page
     #(controller/check-box-clicked pagelist docs-pane edit-checkbox))

    (utils/add-listbox-listener
     pagelist
     #(controller/page-selected pagelist docs-pane edit-checkbox))


    ;; (.addActionListener
    ;;  edit-checkbox
    ;;  (reify ActionListener
    ;;    (actionPerformed [_ _]
    ;;      (fill-cahiers-panel cahiers-panel pages-panel docs-pane edit-checkbox))))

    ;; initial update
    (update-cahiers booklist pagelist docs-pane  edit-checkbox)

    ;; finish frame configuratin and display it
    (add-menus frame pagelist docs-pane edit-checkbox)
    (doto frame
      (.setBackground FRAME_BG_COLOR)
      (.setDefaultCloseOperation JFrame/DO_NOTHING_ON_CLOSE)
      (.setResizable true)
      (.setLocation (:x location) (:y location))
      (.setSize FRAME-WIDTH FRAME-HEIGHT)
      (.setVisible true))))
