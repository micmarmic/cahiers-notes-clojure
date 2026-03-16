(ns cahiers-notes.controller
  (:require
   [cahiers-notes.data :as data]
   [cahiers-notes.file-utils :as files]
   [cahiers-notes.views.gui-utils :as gui]
   [clojure.java.io :as io]
   [clojure.pprint :as pprint])
  (:import
   [java.io FileNotFoundException]
   [javax.swing JOptionPane]))


(defn init-books
  "Build the books database from the directory structure in the provide root"
  [root-string]
  (let [root-file (io/file root-string)]
    (if-not (.isDirectory root-file)
      {:error (str "Le chemin n'existe pas:" root-string)}
      (let [books (files/get-books-from-disk root-file)]
        (data/set-root-folder! root-file)
        (data/set-books! books)
        {:success "OK"}))))

(defn add-page
  [booklist pagelist _docs-pane _callback]
  (when (not (nil?  (.getSelectedValue booklist)))
    (let [book-title (.getSelectedValue booklist)
          new-page-title (JOptionPane/showInputDialog "Titre: ")
          pagemodel (.getModel pagelist)]
          (when (not= nil new-page-title)
          (cond (data/page-title-exists? new-page-title)
                (gui/show-error "Ce titre de page existe déjà!")
                :else
                (println "TODO ADD PAGE '" new-page-title "' to book '" book-title)
                ;; 
                ;; (let [new-subfolder-result (files/create-subfolder
                ;;                             @data/root-folder
                ;;                             title)]
                ;;   (if (:error new-subfolder-result)
                ;;     (gui/show-error (:error new-subfolder-result))
                ;;     (if-not (data/add-cahier (:success new-subfolder-result))
                ;;       (gui/show-error "Ce titre de livre existe déjà!")
                ;;       (do
                ;;         (.addElement pagemodel title)
                ;;         (.setSelectedIndex pagemodel (dec (.getSize pagemodel)))))))
                        )))))

(defn rename-page
  [_pagelist _callback])

(defn add-cahier
  "Add a cahier folder on disk and add the corresponding data.
   Refresh GUI.
   Display a messagebox if there is an error"
  [booklist]
  (let [title (JOptionPane/showInputDialog "Titre: ")        
        bookmodel (.getModel booklist)]
    ;; TODO validate title: no unlawful chars - it's also a path name
    (when (not= nil title)
      (cond (data/book-title-exists? title)
            (gui/show-error "Ce titre de livre existe déjà!")
            :else
            (let [new-subfolder-result (files/create-subfolder
                                        @data/root-folder
                                        title)]
              (if (:error new-subfolder-result)
                (gui/show-error (:error new-subfolder-result))
                (if-not (data/add-cahier (:success new-subfolder-result))
                  (gui/show-error "Ce titre de livre existe déjà!")
                  (do
                    (.addElement bookmodel title)
                    (.setSelectedIndex booklist (dec (.getSize bookmodel)))))))))))

(defn rename-cahier
  "Rename a cahier on disk and in the data and refresh the guid.
   Display a messagebox if there is an error"
  [booklist gui-update-fn]
  (let [current-title (.getSelectedValue booklist)
        current-book (data/book-for-title current-title)]
    (when (not= nil current-title)
      (let [new-title (JOptionPane/showInputDialog "Titre: " current-title)]
        (when (not= nil new-title)
          (let [new-subfolder-result (files/rename-cahier-folder
                                      (:path current-book)
                                      new-title)]
            (if (:error new-subfolder-result)
              (gui/show-error (:error new-subfolder-result))
              (if-not (data/rename-cahier current-title new-title)
                (gui/show-error "Impossible de renommer ce cahier.")
                (do
                  (swap! data/books assoc (:id current-book) 
                         (data/update-book-title-path current-book new-title (:success new-subfolder-result)))
                  (pprint/pprint @data/books)
                  (gui-update-fn booklist new-title))))))))))

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