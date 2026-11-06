INSERT INTO config (max_quiz_count, max_wordbook_count, max_wordbooks_per_quiz, max_words_per_book)
SELECT 10, 10, 5, 100
WHERE NOT EXISTS (SELECT 1 FROM config);

INSERT INTO quiz_type (id, quiz_type_name, quiz_type_description, use_ai, created_at, updated_at, created_by, last_modified_by)
SELECT 1, 'WORD_TO_MEANING', '단어를 보고 뜻을 맞히는 기본 퀴즈 타입입니다.', false, NOW(), NOW(), 1, 1
WHERE NOT EXISTS (SELECT 1 FROM quiz_type WHERE quiz_type_name = 'WORD_TO_MEANING');

INSERT INTO quiz_type (id, quiz_type_name, quiz_type_description, use_ai, created_at, updated_at, created_by, last_modified_by)
SELECT 2, 'MEANING_TO_WORD', '뜻을 보고 단어를 맞히는 기본 퀴즈 타입입니다.', false, NOW(), NOW(), 1, 1
WHERE NOT EXISTS (SELECT 1 FROM quiz_type WHERE quiz_type_name = 'MEANING_TO_WORD');
