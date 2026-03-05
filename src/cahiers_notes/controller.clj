(ns cahiers-notes.controller
  (:import [javax.swing JOptionPane])
  (:require [cahiers-notes.views.gui-utils :as gui]
            [cahiers-notes.data :as data]))

(defn add-cahier
  "Add a cahier on disk and in the data and refresh the guid.
     Display a messagebox if there is an error"
  [booklist]
  (let [title (JOptionPane/showInputDialog "Titre: ")
        bookmodel (.getModel booklist)]
    (when (not= nil title)
      (if-not (data/add-cahier title)
    ;showInputDialog
        (gui/show-error "Ce titre de livre existe déjà!")    
        (do
          (.addElement bookmodel title)
          (.setSelectedIndex booklist (dec (.getSize bookmodel))))))))

(defn rename-cahier
  "Rename a cahier on disk and in the data and refresh the guid.
   Display a messagebox if there is an error"
  [booklist gui-update-fn]
  (let [current-title (.getSelectedValue booklist)]
    (when (not= nil current-title)
      (let [new-title (JOptionPane/showInputDialog "Titre: " current-title)]
        (when (not= nil new-title)
          (if-not (data/rename-cahier current-title new-title)
            (gui/show-error "Ce titre de livre existe déjà!")
            (gui-update-fn booklist new-title)))))))
