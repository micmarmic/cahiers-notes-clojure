(ns cahiers-notes.file-utils
  (:import [java.nio.file Files Paths])
  (:require [clojure.java.io :as io]
            [cahiers-notes.data :as data]))

(defn file-not-dot?
  "Given a File, return true if it's a file and the
   name doesn't start with '.', else return false"
  [path-file]
  (and (not (.isDirectory path-file))
       (not (.startsWith (.getName path-file) "."))))

(defn directory-not-dot?
  "Given a File, return true if it's a file and the
   name doesn't start with '.', else return false"
  [path-file]
  (and (.isDirectory path-file)
       (not (.startsWith (.getName path-file) "."))))

(defn get-pages-from-disk
  "Given a File directory name, return the files it contains. 
   Ignore directories."
  [dir-file]
  (let [files (filter file-not-dot? (.listFiles dir-file))]
    (for [file files]
      {:id (data/make-guid) :title (.getName file) :path file})))

(defn get-books-from-disk
  "Given io/file root folder, return a seq of the sub-directories it contains."
  [root-file]
  (let [sub-dirs (filter directory-not-dot? (.listFiles root-file))]
    (loop [dirs sub-dirs
           books {}]
      (if (empty? dirs) books
          (let [id (data/make-guid)
                book-folder (first dirs)
                pages (get-pages-from-disk book-folder)
                book {:id id
                      :title (.getName book-folder)
                      :path book-folder
                      :pages pages}]
            (recur (rest dirs) (assoc books id book)))))))

(defn create-subfolder
  "Create a folder.
   Success: return {:success <File for path>} 
   Failure: {:error message}
   The root argument must be a File and the name a string."
  [root name]
  (let [newpath (io/file (str (.getAbsolutePath root) "/" name))]
    (try
      (if (or (not (.mkdir (io/file newpath)))
              ; what is really created?
              (.isDirectory newpath))
        {:success newpath}
        {:error (str "Le répertoire n'a pas été créé:\n" newpath)})
      (catch Exception e
        (.printStackTrace e)))))

(defn- move-folder 
  "Move the folder to the new name in the same root.
   Return the new folder-file"
  [folder-file new-name]
    (let [nio-path (Paths/get (.getAbsolutePath folder-file) (into-array String []))
        parent (.getParent nio-path)
        new-nio-path (Paths/get (str parent "/" new-name) (into-array String []))]
    (Files/move nio-path new-nio-path (into-array java.nio.file.CopyOption []))))

(defn rename-cahier-folder
  "Given a io/file and a title, rename it on disk.
   Success: return {:success <File for new path>} 
   Failure: {:error message}
   The root argument must be a File and the name a string."
  [current-folder new-name]
  (try
    (let [newpath-file (move-folder current-folder new-name)]
      {:success newpath-file})
    (catch Exception e
      {:error (str "Impossible de renommer ce" 
                   " cahier sur le disque. Le nom doit être un nom"
                   " de répertoire valide.n" (.getMessage e))})))

(comment 
  (def source-folder-file (io/file "/home/michel/Downloads/TEMP"))    
  (def new-name "TEMP2")
  (let [nio-path (Paths/get (.getAbsolutePath source-folder-file) (into-array String []))
        parent (.getParent nio-path)
        new-nio-path (Paths/get (str parent "/" new-name) (into-array String []))
        ]
    (Files/move nio-path new-nio-path (into-array java.nio.file.CopyOption []))
    )
  (rename-cahier-folder source-folder-file "*/!")
  )