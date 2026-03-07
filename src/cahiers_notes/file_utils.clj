(ns cahiers-notes.file-utils
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
  "Given a string directory name, return a seq of sub-directory"
  [root]
  (let [sub-dirs (filter directory-not-dot? (.listFiles (io/file root)))]
    (loop [dirs sub-dirs
           books {}]
      (if (empty? dirs) books
          (let [id (data/make-guid)
                book-folder (first dirs)
                pages (get-pages-from-disk book-folder)
                book {:id (data/make-guid)
                      :title (.getName book-folder)
                      :path book-folder
                      :pages pages}]
            (recur (rest dirs) (assoc books id book)))))))
