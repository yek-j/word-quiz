package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.auth.JwtTokenProvider;
import com.jyk.wordquiz.wordquiz.common.exception.AuthenticatedUserNotFoundException;
import com.jyk.wordquiz.wordquiz.common.exception.WordBookNotFoundException;
import com.jyk.wordquiz.wordquiz.model.dto.request.WordBookRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.WordBooks;
import com.jyk.wordquiz.wordquiz.model.dto.response.WordBooksResponse;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.model.entity.WordBook;
import com.jyk.wordquiz.wordquiz.repository.UserRepository;
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
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenProvider provider;

    public WordBooksResponse getWordBooks(String token, int page, String criteria, String sort) {
        Long userId = provider.getSubject(token);
        User user =  userRepository.findById(userId).orElseThrow(() -> new AuthenticatedUserNotFoundException(userId));

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
        User user =  userRepository.findById(userId).orElseThrow(() -> new AuthenticatedUserNotFoundException(userId));

        WordBook newWordBook = new WordBook();
        newWordBook.setName(wordBookReq.getName());
        newWordBook.setDescription(wordBookReq.getDescription());

        user.addWordBook(newWordBook);

        wordBookRepository.save(newWordBook);
    }

    @Transactional
    public void updateWordBook(Long id, WordBookRequest wordBookReq, String token) {
        Long userId = provider.getSubject(token);
        User user =  userRepository.findById(userId).orElseThrow(() -> new AuthenticatedUserNotFoundException(userId));

        WordBook wordBook = wordBookRepository.findByIdAndCreatedBy(id, user).orElseThrow(() -> new WordBookNotFoundException(id));

        if(wordBookReq.getName().isEmpty()) {
            throw new IllegalArgumentException("단어장의 이름이 비어있습니다.");
        }

        wordBook.setName(wordBookReq.getName());
        wordBook.setDescription(wordBookReq.getDescription());

        wordBookRepository.save(wordBook);
    }

    @Transactional
    public void deleteWordBook(Long id, String token) {
        Long userId = provider.getSubject(token);
        User user =  userRepository.findById(userId).orElseThrow(() -> new AuthenticatedUserNotFoundException(userId));

        WordBook wordBook = wordBookRepository.findByIdAndCreatedBy(id, user).orElseThrow(() -> new WordBookNotFoundException(id));

        wordBookRepository.delete(wordBook);
    }
}
