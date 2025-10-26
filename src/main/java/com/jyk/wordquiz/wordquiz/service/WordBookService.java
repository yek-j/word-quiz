package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.exception.WordBookNotFoundException;
import com.jyk.wordquiz.wordquiz.model.dto.request.WordBookRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.WordBooks;
import com.jyk.wordquiz.wordquiz.model.dto.response.WordBooksResponse;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.model.entity.WordBook;
import com.jyk.wordquiz.wordquiz.repository.WordBookRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class WordBookService {
    @Autowired
    private WordBookRepository wordBookRepository;

    /**
     * 단어장 리스트 가져오기
     * @param user: 사용자
     * @param page: 페이지 번호
     * @param criteria: 기준
     * @param sort: 정렬
     * @return WordBooksResponse: 단어장 리스트
     */
    public WordBooksResponse getWordBooks(User user, int page, String criteria, String sort) {
        Sort.Direction direction = Sort.Direction.DESC;

        if(sort.equals("ASC")) {
            direction = Sort.Direction.ASC;
        }

        Pageable pageReq = PageRequest.of(page, 10, Sort.by(direction, criteria));

        Page<WordBook> findWordBooks = wordBookRepository.findByCreatedBy(user, pageReq);

        Page<WordBooks> pageWordBooks = findWordBooks.map(w -> new WordBooks(
                w.getId(), w.getName(), w.getDescription(), user.getUsername(), user.getId(), w.getCreatedAt()
        ));

        int totalPages = pageWordBooks.getTotalPages();
        List<WordBooks> wordBooks = pageWordBooks.getContent();

        return new WordBooksResponse(wordBooks, totalPages);
    }

    /**
     * 단어장 저장
     * @param wordBookReq: 단어장 정보
     * @param user: 사용자
     */
    @Transactional
    public void saveWordBook(WordBookRequest wordBookReq, User user) {
        WordBook newWordBook = new WordBook();
        newWordBook.setName(wordBookReq.getName());
        newWordBook.setDescription(wordBookReq.getDescription());

        user.addWordBook(newWordBook);

        wordBookRepository.save(newWordBook);
    }

    /**
     * 단어장 수정
     * @param id: 단어장 ID
     * @param wordBookReq: 수정할 단어장 정보
     * @param user: 사용자
     */
    @Transactional
    public void updateWordBook(Long id, WordBookRequest wordBookReq, User user) {
        WordBook wordBook = wordBookRepository.findByIdAndCreatedBy(id, user).orElseThrow(() -> new WordBookNotFoundException(id));

        if(wordBookReq.getName().isEmpty()) {
            throw new IllegalArgumentException("단어장의 이름이 비어있습니다.");
        }

        wordBook.setName(wordBookReq.getName());
        wordBook.setDescription(wordBookReq.getDescription());

        wordBookRepository.save(wordBook);
    }

    /**
     * 단어장 삭제
     * @param id: 단어장 ID
     * @param user: 사용자
     */
    @Transactional
    public void deleteWordBook(Long id, User user) {
        WordBook wordBook = wordBookRepository.findByIdAndCreatedBy(id, user).orElseThrow(() -> new WordBookNotFoundException(id));

        wordBookRepository.delete(wordBook);
    }
}
