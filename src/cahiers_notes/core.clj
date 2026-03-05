(ns cahiers-notes.core
  (:gen-class)
  (:import [javax.swing SwingUtilities])
  (:require [cahiers-notes.views.main-view :as mainview]))

(defn -main
  "I don't do a whole lot ... yet."
  []
  (println "Application Cahiers de note") 
  (SwingUtilities/invokeLater mainview/create-and-show-gui))
