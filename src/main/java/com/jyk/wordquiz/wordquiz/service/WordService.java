package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.auth.JwtTokenProvider;
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
import com.jyk.wordquiz.wordquiz.repository.UserRepository;
import com.jyk.wordquiz.wordquiz.repository.WordBookRepository;
import com.jyk.wordquiz.wordquiz.repository.WordRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private WordRepository wordRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WordBookRepository wordBookRepository;
    @Autowired
    private JwtTokenProvider provider;

    public WordsResponse getWords(Long wordBookId, String token, int page, String criteria, String sort) throws AccessDeniedException {
        Long userId = provider.getSubject(token);
        User user =  userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다. 다시 로그인해 주세요."));

        Sort.Direction direction = Sort.Direction.DESC;

        if(sort.equals("ASC")) {
            direction = Sort.Direction.ASC;
        }

        Pageable pageReq = PageRequest.of(page, 10, Sort.by(direction, criteria));

        // WordBook 권한확인
        WordBook wordBook = wordBookRepository.findById(wordBookId)
                .orElseThrow(() -> new WordBookNotFoundException(wordBookId));

        if(!wordBook.getCreatedBy().equals(user)) {
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

    @Transactional
    public void saveWord(Long wordBookId, WordRequest wordReq, String token) throws AccessDeniedException {
        Long userId = provider.getSubject(token);
        User user =  userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다. 다시 로그인해 주세요."));

        // 단어장
        WordBook wordBook = wordBookRepository.findById(wordBookId)
                .orElseThrow(() -> new WordBookNotFoundException(wordBookId));

        if(!wordBook.getCreatedBy().equals(user)) {
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

        wordBook.addWord(word);

        wordRepository.save(word);
    }

    @Transactional
    public void updateWord(Long wordBookId, Long wordId, UpdateWordRequest updateWordReq, String token) throws AccessDeniedException {
        Long userId = provider.getSubject(token);
        User user =  userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다. 다시 로그인해 주세요."));

        // 단어장 권한 확인
        WordBook wordBook = wordBookRepository.findById(wordBookId)
                .orElseThrow(() -> new WordBookNotFoundException(wordBookId));

        if(!wordBook.getCreatedBy().equals(user)) {
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

    public void deleteWord(Long wordBookId, Long wordId, String token) throws AccessDeniedException {
        Long userId = provider.getSubject(token);
        User user =  userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다. 다시 로그인해 주세요."));

        // 단어장 권한 확인
        WordBook wordBook = wordBookRepository.findById(wordBookId)
                .orElseThrow(() -> new WordBookNotFoundException(wordBookId));

        if(!wordBook.getCreatedBy().equals(user)) {
            throw new AccessDeniedException("이 단어장에 대한 접근 권한이 없습니다.");
        }

        Word word = wordRepository.findByIdAndWordBook(wordId, wordBook).orElseThrow(() -> new WordNotFoundException(wordId));

        wordRepository.delete(word);
    }

    public WordCheckResponse wordCheck(WordCheckRequest wordCheckReq, Long wordBookId, String token) throws AccessDeniedException {
        Long userId = provider.getSubject(token);
        User user =  userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다. 다시 로그인해 주세요."));

        // 단어장 권한 확인
        WordBook wordBook = wordBookRepository.findById(wordBookId)
                .orElseThrow(() -> new WordBookNotFoundException(wordBookId));

        if(!wordBook.getCreatedBy().equals(user)) {
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

    @Transactional
    public List<Words> saveExcelData(Map<String, String> words, Long wordBookId, String token) throws AccessDeniedException {
        Long userId = provider.getSubject(token);
        User user =  userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다. 다시 로그인해 주세요."));

        // 단어장 권한 확인
        WordBook wordBook = wordBookRepository.findById(wordBookId)
                .orElseThrow(() -> new WordBookNotFoundException(wordBookId));

        if(!wordBook.getCreatedBy().equals(user)) {
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
