(ns cahiers-notes.core
  (:gen-class)
  (:require
   [cahiers-notes.controller :as controller]
   [cahiers-notes.views.gui-utils :as gui]
   [cahiers-notes.views.main-view :as mainview])
  (:import
   [javax.swing SwingUtilities]))

(defn -main
  "I don't do a whole lot ... yet."
  []
  (println "Application Cahiers de note")
  ;; TODO: don't hard-code root path
  (let [result 
        (controller/init-books "/home/michel/veraencrypted/Chiffré/DATA_FOR_APPS/CahiersProd")
        error-message (:error result)]
    (if-not error-message
      (SwingUtilities/invokeLater mainview/create-and-show-gui)
      (do
        (gui/show-error (str "Impossible de lire les cahiers:\n" error-message))
        (println "Impossible de lires les cahiers:" (:error result))))))
