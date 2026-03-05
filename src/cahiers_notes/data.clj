(ns cahiers-notes.data)


(def books (atom nil))



;; book titles must be unique
(defn init-books []
  (reset!
   books
   [{:title "Book 1"
     :pages [{:id "b1p1" :title "b1page1" :content "This is hard coded b1page1"}
             {:id "b1p2" :title "b1page2" :content (str "# Titre\nTexte normal\n## Sous-titre\n"
                                                        "- item 1\n"
                                                        "  - item 1.1\n")}]}
    {:title ")Book 2"
     :pages [{:id "b1p1" :title "b2page1" :content "Still hard b2p1"}
             {:id "b2p2" :title "b2page2" :content "yea, yea, b2p2"}]}]))

(init-books)

(defn book-titles []
  (map :title @books))

(defn pages-for-book-title [book-title]
  (:pages (first (filter #(= book-title (:title %)) @books))))

;; (defn content-for-page-id [book-title]
;;   (map :title (:pages (first (filter #(= book-title (:title %)) @books)))))





