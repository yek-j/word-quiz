package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.auth.JwtTokenProvider;
import com.jyk.wordquiz.wordquiz.model.dto.request.WordBookRequest;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.model.entity.WordBook;
import com.jyk.wordquiz.wordquiz.repository.UserRepository;
import com.jyk.wordquiz.wordquiz.repository.WordBookRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WordBookService {
    @Autowired
    private WordBookRepository wordBookRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenProvider provider;

    @Transactional
    public void saveWordBook(WordBookRequest wordBookReq, String token) {
        Long userId = provider.getSubject(token);
        Optional<User> findUser =  userRepository.findById(userId);

        if(findUser.isEmpty()) {
            throw new EntityNotFoundException("사용자 정보를 찾을 수 없습니다. 다시 로그인해 주세요.");
        }

        WordBook newWordBook = new WordBook();
        newWordBook.setName(wordBookReq.getName());
        newWordBook.setDescription(wordBookReq.getDescription());

        findUser.get().addWordBook(newWordBook);

        wordBookRepository.save(newWordBook);
    }
}
