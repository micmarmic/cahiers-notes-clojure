(ns cahiers-notes.controller
  (:require
   [cahiers-notes.data :as data]
   [cahiers-notes.file-utils :as files]
   [cahiers-notes.views.gui-utils :as gui]
   [clojure.java.io :as io]
   [markdown-viewer.lib.markdown-panel :as md])
  (:import
   [java.io FileNotFoundException]
   [javax.swing JOptionPane]))

;; used in update-docs-pane to manage edit changes
(def editing-state (atom false))
(def current-page (atom nil))

(defn set-current-page
  [page]
  (reset! current-page page))

(declare save-page)

(defn close-app
  [frame pagelist docs-pane]
  (let [selected-page (.getSelectedValue pagelist)
        path (:path selected-page)
        contents (.getText docs-pane)]
    (when (and (not (nil? selected-page)) @editing-state)
      (save-page path contents)))
  (.dispose frame)
  (System/exit 0))

(defn clear-edit-checkbox-and-state
  "Uncheck the box and update the state variable"
  [checkbox]
  (println "clear-edit-checkbox-and-state")
  (reset! editing-state false)
  (.setSelected checkbox false))

(declare file-contents)

(defn update-docs-panel [pagelist docs-pane  edit-checkbox]
  (let [selected-page (.getSelectedValue pagelist)
        edit? (.isSelected edit-checkbox)
        contents-result (if (not= selected-page nil)
                          (file-contents (:path selected-page))
                          {:success ""})
        contents (:success contents-result)]
    
    (.setEditable docs-pane edit?)

    (when (not (:error contents-result))
      (md/clear-all-styles docs-pane)
      (md/clear-document docs-pane)
      (if edit?
        (.setText docs-pane contents)
        (md/add-text docs-pane contents)))))

(defn file-contents
  "Return the {:success contents} of the given File or {:error message}
   If there is an error, display a message and return and empty string"
  [file]
  (try
    {:success (slurp file)}
    (catch FileNotFoundException e
      (let [message (str "Impossible de lire cette page:\n" e)]
        (gui/show-error message)
        {:error message}))))
 
(defn save-doc-disable-edit
  "Save the current document and turn off the edit"
  [pagelist docs-pane edit-checkbox]
  (let [selected-page (.getSelectedValue pagelist)]
    (when (and selected-page @editing-state)
      (save-page (:path selected-page) (.getText docs-pane)))
    (clear-edit-checkbox-and-state edit-checkbox)))

(defn save-page
  "save the page text to file"
  [path text]
  (try
    (spit path text)
    (catch Exception e
      (gui/show-error (str "Erreur durant la sauvegarde:\n" e)))))


(defn menu-toggle-edit
  [edit-checkbox]
  (println "menu toggle edit")
  (.doClick edit-checkbox))

(defn check-box-clicked
  "Process actions required when edit-check-box is clicked"
  [pagelist docs-pane edit-checkbox]
  ; when editing and edit is toggle off, save the page
  (let [new-state (.isSelected edit-checkbox)
        current-page (.getSelectedValue pagelist)]
    (when (= new-state false)
      ; we were editing so now save the changes
      (save-page (:path current-page) (.getText docs-pane)))    
    (update-docs-panel pagelist docs-pane edit-checkbox)
    (.setEditable docs-pane new-state)
    (reset! editing-state new-state)))

(defn page-selected
  "Perform actions when a page is selected"
  [pagelist docs-pane edit-checkbox]
  (println "page-selected")
  (let [selected-page (.getSelectedValue pagelist)]
    (println "selected-page" selected-page)
    (println "editing-stage" @editing-state)

    ; save document being edited and change edit state
    (when (and selected-page @editing-state)
      (println "save edited doc" current-page)
      (clear-edit-checkbox-and-state edit-checkbox)
      (save-doc-disable-edit pagelist docs-pane edit-checkbox))

    (set-current-page selected-page))
  (update-docs-panel pagelist docs-pane  edit-checkbox))

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
          new-page-title (gui/inputFromUser "Ajouter une page" "Titre:")]
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

(defn rename-page
  [_pagelist _callback])

(defn add-cahier
  "Add a cahier folder on disk and add the corresponding data.
   Refresh GUI.
   Display a messagebox if there is an error"
  [booklist]
  (let [title (gui/inputFromUser "Ajouter un cahier" "Titre: ")
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
  [booklist pagelist docs-pane edit-checkbox  gui-update-fn]
  (let [current-title (.getSelectedValue booklist)
        current-book (data/book-for-title current-title)]
    (when (not= nil current-title)
      ;; MUST save the page (and flip the editing state
      ;; before renaming the folder else the save will fail
      (save-doc-disable-edit pagelist docs-pane edit-checkbox)

      (let [new-title (gui/inputFromUser "Renommer un cahier" "Titre: " current-title)]
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

