package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.exception.DuplicationWordException;
import com.jyk.wordquiz.wordquiz.common.exception.WordBookNotFoundException;
import com.jyk.wordquiz.wordquiz.common.exception.WordNotFoundException;
import com.jyk.wordquiz.wordquiz.model.dto.request.UpdateWordRequest;
import com.jyk.wordquiz.wordquiz.model.dto.request.WordCheckRequest;
import com.jyk.wordquiz.wordquiz.model.dto.request.WordRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.WordCheckResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.Words;
import com.jyk.wordquiz.wordquiz.model.dto.response.WordsResponse;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.model.entity.Word;
import com.jyk.wordquiz.wordquiz.model.entity.WordBook;
import com.jyk.wordquiz.wordquiz.repository.WordBookRepository;
import com.jyk.wordquiz.wordquiz.repository.WordRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.*;

@Service
@Slf4j
public class WordService {
    private final WordRepository wordRepository;
    private final WordBookRepository wordBookRepository;

    public WordService(WordRepository wordRepository, WordBookRepository wordBookRepository) {
        this.wordRepository = wordRepository;
        this.wordBookRepository = wordBookRepository;
    }

    /**
     * 단어 목록 조회
     * @param wordBookId: 단어장 ID
     * @param user: 사용자
     * @param page: 페이지
     * @param criteria: 기준
     * @param sort: 정렬
     * @return WordsResponse 반환
     * @throws AccessDeniedException
     */
    public WordsResponse getWords(Long wordBookId, User user, int page, String criteria, String sort) throws AccessDeniedException {
        Sort.Direction direction = Sort.Direction.DESC;

        if(sort.equals("ASC")) {
            direction = Sort.Direction.ASC;
        }

        Pageable pageReq = PageRequest.of(page, 10, Sort.by(direction, criteria));

        // WordBook 권한확인
        WordBook wordBook = wordBookRepository.findById(wordBookId)
                .orElseThrow(() -> new WordBookNotFoundException(wordBookId));

        if(!wordBook.getCreatedBy().getId().equals(user.getId())) {
            throw new AccessDeniedException("이 단어장에 대한 접근 권한이 없습니다.");
        }

        Page<Word> findWords = wordRepository.findByWordBook(wordBook, pageReq);

        Page<Words> pageWords = findWords.map(w -> new Words(
           w.getId(), w.getTerm(), w.getDescription(), w.getCreatedAt()
        ));

        int totalPages = pageWords.getTotalPages();
        List<Words> words = pageWords.getContent();

        return new WordsResponse(words, totalPages);
    }

    /**
     * 단어 저장
     * @param wordBookId: 단어장 ID
     * @param wordReq: 단어 정보
     * @param user: 사용자
     * @throws AccessDeniedException
     */
    @Transactional
    public void saveWord(Long wordBookId, WordRequest wordReq, User user) throws AccessDeniedException {
        // 단어장
        WordBook wordBook = wordBookRepository.findById(wordBookId)
                .orElseThrow(() -> new WordBookNotFoundException(wordBookId));

        if(!wordBook.getCreatedBy().getId().equals(user.getId())) {
            throw new AccessDeniedException("이 단어장에 대한 접근 권한이 없습니다.");
        }

        // 중복 검증
        Word duplcheck = wordDuplication(wordReq.getTerm(), wordBook);

        if(duplcheck != null) {
            throw new DuplicationWordException(duplcheck.getTerm(), duplcheck.getDescription());
        }

        // 단어
        Word word = new Word();
        word.setTerm(wordReq.getTerm());
        word.setDescription(wordReq.getDescription());
        word.setWordBook(wordBook);

        wordRepository.save(word);
    }

    /**
     * 단어 수정
     * @param wordBookId: 단어장 ID
     * @param wordId: 단어 ID
     * @param updateWordReq: 수정할 단어 정보
     * @param user: 사용자
     * @throws AccessDeniedException
     */
    @Transactional
    public void updateWord(Long wordBookId, Long wordId, UpdateWordRequest updateWordReq, User user) throws AccessDeniedException {
        // 단어장 권한 확인
        WordBook wordBook = wordBookRepository.findById(wordBookId)
                .orElseThrow(() -> new WordBookNotFoundException(wordBookId));

        if(!wordBook.getCreatedBy().getId().equals(user.getId())) {
            throw new AccessDeniedException("이 단어장에 대한 접근 권한이 없습니다.");
        }

        Word word = wordRepository.findById(wordId).orElseThrow(() -> new WordNotFoundException(wordId));

        word.setTerm(updateWordReq.getTerm());
        word.setDescription(updateWordReq.getDescription());

        // 단어장 변경
        Long moveWordBookId = updateWordReq.getWordBookId();

        if(!Objects.equals(moveWordBookId, wordBookId)) {
            WordBook newWordBook = wordBookRepository.findById(moveWordBookId)
                    .orElseThrow(() -> new WordBookNotFoundException(moveWordBookId));

            if(!newWordBook.getCreatedBy().equals(user)) {
                throw new AccessDeniedException("이동하려는 단어장에 대한 접근 권한이 없습니다.");
            }

            word.setWordBook(newWordBook);
        }

        wordRepository.save(word);
    }

    /**
     * 단어 삭제
     * @param wordBookId: 단어장 ID
     * @param wordId: 단어 ID
     * @param user: 사용자
     * @throws AccessDeniedException
     */
    public void deleteWord(Long wordBookId, Long wordId, User user) throws AccessDeniedException {
        // 단어장 권한 확인
        WordBook wordBook = wordBookRepository.findById(wordBookId)
                .orElseThrow(() -> new WordBookNotFoundException(wordBookId));

        if(!wordBook.getCreatedBy().getId().equals(user.getId())) {
            throw new AccessDeniedException("이 단어장에 대한 접근 권한이 없습니다.");
        }

        Word word = wordRepository.findByIdAndWordBook(wordId, wordBook).orElseThrow(() -> new WordNotFoundException(wordId));

        wordRepository.delete(word);
    }

    /**
     * 단어 중복 체크
     * @param wordCheckReq: 중복체크할 단어
     * @param wordBookId: 단어장 ID
     * @param user: 사용자
     * @return WordCheckResponse 단어 중복 체크 여부
     * @throws AccessDeniedException
     */
    public WordCheckResponse wordCheck(WordCheckRequest wordCheckReq, Long wordBookId, User user) throws AccessDeniedException {
        // 단어장 권한 확인
        WordBook wordBook = wordBookRepository.findById(wordBookId)
                .orElseThrow(() -> new WordBookNotFoundException(wordBookId));

        if(!wordBook.getCreatedBy().getId().equals(user.getId())) {
            throw new AccessDeniedException("이 단어장에 대한 접근 권한이 없습니다.");
        }

        Word word = wordDuplication(wordCheckReq.getTerm(), wordBook);
        if(word != null) {
            return new WordCheckResponse(true, word.getTerm(), word.getDescription());
        }

        return new WordCheckResponse(false, "", "");
    }

    private Word wordDuplication(String term, WordBook wordBook) {
        Optional<Word> word = wordRepository.findByTermAndWordBook(term, wordBook);

        return word.orElse(null);
    }

    /**
     * 엑셀로 단어 데이터 저장
     * @param words: 저장할 단어
     * @param wordBookId: 단어장 ID
     * @param user: 사용자
     * @return 저장된 단어 List
     * @throws AccessDeniedException
     */
    @Transactional
    public List<Words> saveExcelData(Map<String, String> words, Long wordBookId, User user) throws AccessDeniedException {
        // 단어장 권한 확인
        WordBook wordBook = wordBookRepository.findById(wordBookId)
                .orElseThrow(() -> new WordBookNotFoundException(wordBookId));

        if(!wordBook.getCreatedBy().getId().equals(user.getId())) {
            throw new AccessDeniedException("이 단어장에 대한 접근 권한이 없습니다.");
        }

        List<Word> newWords = new ArrayList<>();
        List<Words> dupleWords = new ArrayList<>();

        for(String term : words.keySet()) {
            Word duplWord = wordDuplication(term, wordBook);

            if (duplWord != null) {
                dupleWords.add(new Words(duplWord.getId(), duplWord.getTerm(), duplWord.getDescription(), duplWord.getCreatedAt()));
                continue;
            }
            Word newWord = new Word();
            newWord.setTerm(term);
            newWord.setDescription(words.get(term));
            newWord.setWordBook(wordBook);
            newWords.add(newWord);
        }

        wordRepository.saveAll(newWords);

        if (!dupleWords.isEmpty()) {
            return dupleWords;
        }

        return null;
    }
}
