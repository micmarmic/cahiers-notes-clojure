(ns cahiers-notes.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [cahiers-notes.data :as data]))

(def books
  [{:title "Test 1"
    :pages [{:id "b1p1" :title "b1page1" :content "This is hard coded b1page1"}
            {:id "b1p2" :title "b1page2" :content (str "# Titre\nTexte normal\n## Sous-titre\n"
                                                       "- item 1\n"
                                                       "  - item 1.1\n")}]}
   {:title "Test 2"
    :pages [{:id "b1p1" :title "b2page1" :content "Still hard b2p1"}
            {:id "b2p2" :title "b2page2" :content "yea, yea, b2p2"}]}])

(deftest book-title-exists-returns-the-correct-result
  (testing
   (is (data/book-title-exists? "Test 1" books) "Book Test 1 exists")
    (is (not (data/book-title-exists? "eahre" books)) "This book does not exist")))
