(ns cahiers-notes.data)


(def books (atom nil))


(defn init-books []
  (reset!
   books
   [{:title "Book 1"
     :pages [{:title "b1page1"}
             {:title "b1page2"}]}
    {:title "Book 2"
     :pages [{:title "b2page1"}
             {:title "b2page2"}]}]))

(init-books)

(defn book-titles []
  (map :title @books))

(defn pages-for-book [book-title]
  (map :title (:pages (first (filter #(= book-title (:title %)) @books)))))




