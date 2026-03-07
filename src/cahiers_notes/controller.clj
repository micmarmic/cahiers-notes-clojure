(ns cahiers-notes.controller
  (:require
   [cahiers-notes.data :as data]
   [cahiers-notes.file-utils :as files]
   [cahiers-notes.views.gui-utils :as gui]
   [clojure.java.io :as io])
  (:import
   [java.io FileNotFoundException]
   [javax.swing JOptionPane]))


(defn init-books
  "Build the books database from the directory structure in the provide root"
  [root]
  ;
  (if-not (.isDirectory (io/file root))
    {:error (str "Le chemin n'existe pas:" root)}
    (let [books (files/get-books-from-disk root)]
      (println "count books" (count books))
      (data/set-books! books)
      {:success "OK"})))

(comment
  (def cahier-path "/home/michel/veraencrypted/Chiffré/DATA_FOR_APPS/CahiersProd")
  (init-books cahier-path)
  @data/books
  (doseq [title (data/book-titles)]
    (println title)))


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

(defn file-contents
  "Return the contents of the given File.
   If there is an error, display a message and return and empty string"
  [file]
  (try
    (slurp file :encoding "ISO-8859-1")
    (catch FileNotFoundException e
      (let [message (str "Impossible de lire cette page:\n" e)]
        (gui/show-error message)))))

(comment 
  (def file (io/file "/home/michel/veraencrypted/Chiffré/DATA_FOR_APPS/CahiersProd/Programmation/pyinstaller.txt"))
  (file-contents file)
  )


