package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.auth.JwtTokenProvider;
import com.jyk.wordquiz.wordquiz.common.exception.WordBookNotFoundException;
import com.jyk.wordquiz.wordquiz.model.dto.request.WordBookRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.WordBooks;
import com.jyk.wordquiz.wordquiz.model.dto.response.WordBooksResponse;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.model.entity.WordBook;
import com.jyk.wordquiz.wordquiz.repository.UserRepository;
import com.jyk.wordquiz.wordquiz.repository.WordBookRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class WordBookService {
    @Autowired
    private WordBookRepository wordBookRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenProvider provider;

    public WordBooksResponse getWordBooks(String token, int page, String criteria, String sort) {
        Long userId = provider.getSubject(token);
        User user =  userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다. 다시 로그인해 주세요."));

        Sort.Direction direction = Sort.Direction.DESC;

        if(sort.equals("ASC")) {
            direction = Sort.Direction.ASC;
        }

        Pageable pageReq = PageRequest.of(page, 10, Sort.by(direction, criteria));

        Page<WordBook> findWordBooks = wordBookRepository.findByCreatedBy(user, pageReq);

        Page<WordBooks> pageWordBooks = findWordBooks.map(w -> new WordBooks(
                w.getId(), w.getName(), w.getDescription(), user.getUsername(), userId, w.getCreatedAt()
        ));

        int totalPages = pageWordBooks.getTotalPages();
        List<WordBooks> wordBooks = pageWordBooks.getContent();

        return new WordBooksResponse(wordBooks, totalPages);
    }

    @Transactional
    public void saveWordBook(WordBookRequest wordBookReq, String token) {
        Long userId = provider.getSubject(token);
        User user =  userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다. 다시 로그인해 주세요."));

        WordBook newWordBook = new WordBook();
        newWordBook.setName(wordBookReq.getName());
        newWordBook.setDescription(wordBookReq.getDescription());

        user.addWordBook(newWordBook);

        wordBookRepository.save(newWordBook);
    }

    @Transactional
    public void updateWordBook(Long id, WordBookRequest wordBookReq, String token) throws IllegalAccessException {
        Long userId = provider.getSubject(token);
        User user =  userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다. 다시 로그인해 주세요."));

        WordBook wordBook = wordBookRepository.findByIdAndCreatedBy(id, user).orElseThrow(() -> new WordBookNotFoundException(id));

        if(wordBookReq.getName().isEmpty()) {
            throw new IllegalAccessException("단어장의 이름이 비어있습니다.");
        }

        wordBook.setName(wordBookReq.getName());
        wordBook.setDescription(wordBookReq.getDescription());

        wordBookRepository.save(wordBook);
    }

    @Transactional
    public void deleteWordBook(Long id, String token) {
        Long userId = provider.getSubject(token);
        User user =  userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다. 다시 로그인해 주세요."));

        WordBook wordBook = wordBookRepository.findByIdAndCreatedBy(id, user).orElseThrow(() -> new WordBookNotFoundException(id));

        wordBookRepository.delete(wordBook);
    }
}
