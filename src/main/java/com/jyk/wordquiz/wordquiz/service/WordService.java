package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.auth.JwtTokenProvider;
import com.jyk.wordquiz.wordquiz.common.exception.WordBookNotFoundException;
import com.jyk.wordquiz.wordquiz.model.dto.request.WordRequest;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.model.entity.Word;
import com.jyk.wordquiz.wordquiz.model.entity.WordBook;
import com.jyk.wordquiz.wordquiz.repository.UserRepository;
import com.jyk.wordquiz.wordquiz.repository.WordBookRepository;
import com.jyk.wordquiz.wordquiz.repository.WordRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.Optional;

@Service
public class WordService {
    @Autowired
    private WordRepository wordRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WordBookRepository wordBookRepository;
    @Autowired
    private JwtTokenProvider provider;

    @Transactional
    public void saveWord(Long wordBookId, WordRequest wordReq, String token) throws AccessDeniedException {
        Long userId = provider.getSubject(token);
        Optional<User> findUser = userRepository.findById(userId);

        if(findUser.isEmpty()) {
            throw new EntityNotFoundException("사용자 정보를 찾을 수 없습니다. 다시 로그인해 주세요.");
        }

        // 단어장
        WordBook wordBook = wordBookRepository.findById(wordBookId)
                .orElseThrow(() -> new WordBookNotFoundException(wordBookId));

        if(!wordBook.getCreatedBy().equals(findUser.get())) {
            throw new AccessDeniedException("이 단어장에 대한 접근 권한이 없습니다.");
        }

        // 단어
        Word word = new Word();
        word.setTerm(wordReq.getTerm());
        word.setDescription(wordReq.getDescription());

        wordBook.addWord(word);

        wordRepository.save(word);
    }
}
