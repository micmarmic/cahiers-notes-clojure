(ns cahiers-notes.data)


(def books (atom nil))

(def root-folder (atom nil))

(defn set-root-folder!
  [file-path]
  (reset! root-folder file-path))

(defn set-books!
  [books-data]
  (reset! books books-data))

(def GUID-CHARS [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9 \a \b \c \d \e \f])
(def NUM-GUID-CHARS (count GUID-CHARS))

(defn make-guid
  "Return a hopefully unique guid (string).
     NOTE: the guid is a string instead of a keyword because data.json has issues. See the note in models.clj"
  []
  (reduce str (for [_ (range 32)] (get GUID-CHARS (rand-int NUM-GUID-CHARS)))))

(defn book-titles []
  (map :title (vals @books)))

(defn book-title-exists?
  "True if the book title exists. 
   Pass books as argument for testing, else default to @books"
  ([title]
   (book-title-exists? title @books))
  ([title the-books]
   (if (seq (filter #(= (:title %) title) (vals the-books)))
     true
     false)))

(defn add-page-to-book [current-book new-page]
  (let [new-pages (conj (:pages current-book) new-page)]
  (reset! books (assoc-in @books [(:id current-book) :pages] new-pages))))




(comment
  (let [books {:book1 {:title "Title 1" :pages [{:title "allo"}]}
               :book2 {:title "Title 2" :pages [{:title "love"}]}
               :book3 {:title "Title 3" :pages [{:title "toto"}]} 
               :book4 {:title "Title 4" :pages [{:title "robo"}]}}
        title "Title 5"]
    (book-title-exists? title books))

  (let [books {:book1 {:pages [{:title "allo"}]} :book2 {:pages [{:title "love"}]}
               :book3 {:pages [{:title "toto"}]} :book4 {:pages [{:title "robo"}]}}
        title "toao"]
    (if (seq (filter #(= (:title %) title) (flatten (map :pages (vals books)))))
      true
      false))
    ;(pp/pprint books)
  )
  

(defn page-title-exists?
  "True if the page title exists. 
   Pass books as argument for testing, else default to @books.
   Pages are stored in a vector in books."
  ([title]
   (book-title-exists? title @books))
  ([title the-books]
   (if (seq (filter #(= (:title %) title) (flatten (map :pages (vals the-books)))))
      true
      false)))

(defn add-cahier
  "Add the cahier to the data model.
   This is only in memory.
   Return false if the title exists, true otherwise.
   Caller must check that folder exists."
  [directory-file]
  (let [title (.getName directory-file)
        id (make-guid)]
    (if (book-title-exists? title)
      false
      (let [book {:id id :title title :path directory-file :pages []}]
        (reset! books (assoc @books id book))
        true))))

(defn book-id
  "Return the ID for the title or nil if not found"
  [title]
  (first (filter #(= title (:title (get @books %))) (keys @books))))

(defn book-for-title
  "Return the book for the title or nil if not found"
  [title]
  (first (filter #(= title (:title %)) (vals @books))))

(defn pages-for-book-title
  [book-title]
  (:pages (book-for-title book-title)))

(defn update-book-title-path
  "Update the title and path of a book and return it"
  [book new-title new-path]
  {:id (:id book)
   :title new-title
   :path new-path
   :pages (:pages book)})


(defn rename-cahier
  "Rename the cahier with the current title to the new title.
   This is only in memory. The controller creates the corresponding folder.
   Return false if the title exists, true otherwise."
  ;; TODO: this book must exist!
  [current-title new-title]
  (if (book-title-exists? new-title)
    false
    (let [current-book-id (book-id current-title)]
      (reset! books (assoc-in @books [current-book-id :title] new-title))
      true)))

;; (defn pages-for-book-title [book-title]
;;   (:pages (first (filter #(= book-title (:title %)) (vals @books)))))
;; 



;; (defn content-for-page-id [book-title]
;;   (map :title (:pages (first (filter #(= book-title (:title %)) @books)))))





