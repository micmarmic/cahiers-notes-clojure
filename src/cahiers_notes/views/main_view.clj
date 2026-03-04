(ns cahiers-notes.views.main-view
  (:import
   [java.awt Color BorderLayout Dimension FlowLayout]
   [java.awt.event ActionListener]
   [javax.swing
    BorderFactory
    JFrame
    JLabel
    JMenu
    JMenuBar
    JMenuItem
    JPanel
    JSeparator]
   [javax.swing.border EmptyBorder])
  (:require [cahiers-notes.views.gui-utils :as utils]))

(def TITLE "Cahiers")
(def FRAME-WIDTH 1000)
(def FRAME-HEIGHT 800)
(def CAHIER_WIDTH 240)
(def PAGES_WIDTH 240)
(def FRAME_BG_COLOR (Color. 206 187 66))

(def gui (atom {:doc-panel nil :notes-panel nil}))


;; TODO: this may be a generic action listener suitable for all widgets ...
(defn add-action-listener
   [menu-item callback]
   (.addActionListener menu-item (reify ActionListener (actionPerformed [_ _] (callback)))))

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

    ;(add-listener-to-menu file-exit demo-menu-action)))
    (add-action-listener file-exit #(close-app frame))))

(defn create-and-show-gui
  "Build and display the view"
  []
  (let [frame (JFrame. TITLE)
        main-panel (JPanel.)
        left-container (JPanel.)
        cahiers-panel (JPanel.)
        pages-panel (JPanel.)
        docs-panel (JPanel.)
        location (utils/calculate-frame-location FRAME-WIDTH FRAME-HEIGHT)
        temp-label (JLabel. "TEMPO")]

    (reset! gui {:cahiers-panel cahiers-panel :pages-panel pages-panel})

    (doto main-panel
      (.setBackground FRAME_BG_COLOR)
      (.setBorder (EmptyBorder. 10 10 10 10))
      (.setLayout (BorderLayout.)))
    (.add frame main-panel)

    (.add main-panel left-container BorderLayout/WEST)
    (.setLayout left-container (BorderLayout.))

    (doto cahiers-panel
      (.setBackground Color/YELLOW)
      (.setPreferredSize (Dimension. CAHIER_WIDTH (.height (.getPreferredSize cahiers-panel))))
      (.setBorder (BorderFactory/createLineBorder Color/BLACK))
      (.add (JLabel. "CAHIERS")))
    (.add left-container cahiers-panel BorderLayout/WEST)

    (doto pages-panel
      (.setBackground Color/LIGHT_GRAY)
      (.setPreferredSize (Dimension. PAGES_WIDTH (.height (.getPreferredSize pages-panel))))
      (.setBorder (BorderFactory/createLineBorder Color/BLACK))
      (.add (JLabel. "PAGES")))
    (.add left-container pages-panel BorderLayout/EAST)

    (doto docs-panel
      (.setBackground Color/ORANGE)
      (.add (JLabel. "DOCS")))
    (.add main-panel docs-panel FlowLayout/CENTER)

    (add-menus frame)

    (doto frame
      (.setBackground FRAME_BG_COLOR)
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.setResizable true)
      (.setLocation (:x location) (:y location))
      (.setSize FRAME-WIDTH FRAME-HEIGHT)
      (.setVisible true))))
