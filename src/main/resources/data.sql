INSERT INTO config (max_quiz_count, max_wordbook_count, max_wordbooks_per_quiz, max_words_per_book)
SELECT 10, 10, 5, 100
WHERE NOT EXISTS (SELECT 1 FROM config);