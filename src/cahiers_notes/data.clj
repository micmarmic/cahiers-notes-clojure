(ns cahiers-notes.data)


(def books (atom nil))
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


;; (defn add-book
;;   "Add a new directory and update the data."
;;   [bookname]
;;   ; title must be unique
;;   (println bookname)
;;   )

;; ;; book titles must be unique
;; (defn init-books []
;;   (reset!
;;    books
;;    {"a1111"
;;     {:title "Book 1"
;;      :pages [{:id "b1p1" :title "b1page1" :content "This is hard coded b1page1"}
;;              {:id "b1p2" :title "b1page2" :content (str "# Titre\nTexte normal\n## Sous-titre\n"
;;                                                         "- item 1\n"
;;                                                         "  - item 1.1\n")}]}
;;     "b1111"
;;     {:title "Book 2"
;;      :pages [{:id "b1p1" :title "b2page1" :content "Still hard b2p1"}
;;              {:id "b2p2" :title "b2page2" :content "yea, yea, b2p2"}]}}))



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

(defn add-cahier
  "Add the cahier to the data model.
   This is only in memory. The controller creates the corresponding folder.
   Return false if the title exists, true otherwise."
  [title]
  (if (book-title-exists? title)
    false
    (let [book {:title title :pages []}
          id (make-guid)]
      (reset! books (assoc @books id book))
      true)))

(defn book-id 
  "Return the ID for the title or nil if not found"
  [title]
  (first (filter #(= title (:title (get @books %))) (keys @books))))

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

(assoc-in @books ["a1111" :title] "a2223")

(rename-cahier "Book 1" "Book 11")
@books


(defn pages-for-book-title [book-title]
  (:pages (first (filter #(= book-title (:title %)) (vals @books)))))




;; (defn content-for-page-id [book-title]
;;   (map :title (:pages (first (filter #(= book-title (:title %)) @books)))))





