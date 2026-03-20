(ns cahiers-notes.controller
  (:require
   [cahiers-notes.data :as data]
   [cahiers-notes.file-utils :as files]
   [cahiers-notes.views.gui-utils :as gui]
   [clojure.java.io :as io])
  (:import
   [java.io FileNotFoundException]
   [javax.swing JOptionPane]))

;; used in update-docs-pane to manage edit changes
(def editing-state (atom false))

(declare save-page)
(defn close-app
  [frame pagelist docs-pane]
  (println "close-app selected page" (.getSelectedValue pagelist))
  (let [selected-page (.getSelectedValue pagelist)
        path (:path selected-page)
        contents (.getText docs-pane)]
    (when (and (not (nil? selected-page)) @editing-state)
      (println "Save page before close")
      (save-page path contents)))
  (println "Application cahiers fermée.")
  (.dispose frame)
  (System/exit 0))

(defn clear-edit-checkbox-and-state
  "Uncheck the box and update the state variable"
  [checkbox]
  (reset! editing-state false)
  (.setSelected checkbox false))

(defn set-editing-state 
  [state]
  (reset! editing-state state))

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
  "Try to add a new text file to the page folder, update the GUI and database.
   No return. Pop a message if there's an error."
  [booklist pagelist _docs-pane _callback]
  (when (not (nil?  (.getSelectedValue booklist)))
    (let [current-book (data/book-for-title (.getSelectedValue booklist))
          new-page-title (JOptionPane/showInputDialog "Titre: ")]
      (when (not= nil new-page-title)
        (cond (data/page-title-exists? new-page-title)
              (gui/show-error "Ce titre de page existe déjà!")
              :else
              ;; currently no error checking on file creation
              (try 
                (let [page-file
                      (files/create-file (:path current-book) new-page-title)
                      new-page {:id (data/make-guid)
                                :title (str new-page-title ".txt") :path page-file}
                      pages-model (.getModel pagelist)]
                  (data/add-page-to-book current-book new-page)
                  (.addElement pages-model new-page)
                  (.setSelectedIndex pagelist (dec (.getSize pages-model))))
                (catch Exception e 
                  (gui/show-error 
                   (str "Erreur durant la sauvegarde de" new-page-title 
                        "\n"
                        (.getMessage e))))))))))

(defn save-page
  "save the page text to file"
  [path text]
  (println "-------------------------------")
  (println "controller/save-page")
  (println "-------------------------------")
  (println (str "Path '" path "' text '" text '""))
  (println "*******************************")
  (try
    (spit path text)
    (catch Exception e
      (gui/show-error (str "Erreur durant la sauvegarde:\n" e)))))

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

(defn save-doc-disable-edit
  "Save the current document and turn off the edit"
  [pagelist docs-pane edit-checkbox]
  (let [selected-page (.getSelectedValue pagelist)]
    (when (and selected-page @editing-state)
      (save-page (:path selected-page) (.getText docs-pane)))
    (clear-edit-checkbox-and-state edit-checkbox)))

(defn rename-cahier
  "Rename a cahier on disk and in the data and refresh the guid.
   Display a messagebox if there is an error"
  [booklist pagelist docs-pane edit-checkbox  gui-update-fn]
  (let [current-title (.getSelectedValue booklist)
        current-book (data/book-for-title current-title)]
    (when (not= nil current-title)
      ;; MUST save the page (and flip the editing state
      ;; before renaming the folder else the save will fail
      (save-doc-disable-edit pagelist docs-pane edit-checkbox)


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
                  ;; reload all info from disk!! simpler than trying to update paths for pages!
                  (init-books @data/root-folder)
                  (gui-update-fn booklist pagelist docs-pane  edit-checkbox new-title))))))))))

(defn file-contents
  "Return the {:success contents} of the given File or {:error message}
   If there is an error, display a message and return and empty string"
  [file]
  (try
    {:success (slurp file :encoding "ISO-8859-1")}
    (catch FileNotFoundException e
      (let [message (str "Impossible de lire cette page:\n" e)]
        (gui/show-error message)
        {:error message}
        ))))