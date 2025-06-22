package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.auth.JwtTokenProvider;
import com.jyk.wordquiz.wordquiz.common.exception.AuthenticatedUserNotFoundException;
import com.jyk.wordquiz.wordquiz.common.exception.QuizNotFoundException;
import com.jyk.wordquiz.wordquiz.common.type.QuizType;
import com.jyk.wordquiz.wordquiz.common.type.SharingStatus;
import com.jyk.wordquiz.wordquiz.model.dto.request.QuizStartRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizProblem;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizSessionResponse;
import com.jyk.wordquiz.wordquiz.model.entity.*;
import com.jyk.wordquiz.wordquiz.repository.QuizRepository;
import com.jyk.wordquiz.wordquiz.repository.QuizSessionRepository;
import com.jyk.wordquiz.wordquiz.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class QuizSessionService {
    @Autowired
    private JwtTokenProvider provider;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private QuizSessionRepository quizSessionRepository;

    @Transactional
    public QuizSessionResponse startQuiz(String token, QuizStartRequest quizStartReq) {
        Long userId = provider.getSubject(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new AuthenticatedUserNotFoundException(userId));

        // 시작하려는 퀴즈 가져오기
        Quiz quiz = quizRepository.findById(quizStartReq.getQuizId()).orElseThrow(() -> new QuizNotFoundException(quizStartReq.getQuizId()));

        // 본인이 만들었거나 PUBLIC 퀴즈인지 확인
        if(!quiz.getCreatedBy().equals(user) && quiz.getSharingStatus() != SharingStatus.PUBLIC) {
            throw new QuizNotFoundException(quiz.getId());
        }

        // 퀴즈에서 사용하는 Word 가져오기
        List<QuizWordBook> wordbooks = quiz.getQuizWordBooks();

        List<Word> words = new ArrayList<>();

        for (QuizWordBook wb : wordbooks) {
            WordBook wordBook = wb.getWordBook();
            words.addAll(wordBook.getWords());
        }

        // 랜덤
        Collections.shuffle(words);

        // 최대 20개 문제만 선택하여 출제
        List<Word> selectedWords = words.subList(0, Math.min(20, words.size()));

        List<QuizProblem> problemList = new ArrayList<>();

        // QuizSession 생성
        QuizSession quizSession = new QuizSession();
        quizSession.setQuiz(quiz);
        quizSession.setUser(user);
        quizSession.setQuizActive(true);
        quizSession.setQuizType(quizStartReq.getQuizType());

        int i = 1;
        for(Word w : selectedWords) {
            // QuizQuestion 순서대로 문제 추가
            QuizQuestion question = new QuizQuestion();

            question.setWord(w);
            question.setQuestionOrder(i++);
            question.setIsCorrect(null);
            quizSession.addQuestion(question);

            String problem = w.getDescription();
            String answer = w.getTerm();

            if(quizStartReq.getQuizType() == QuizType.WORD_TO_MEANING) {
                problem = w.getTerm();
                answer = w.getDescription();
            }

            QuizProblem qp = new QuizProblem(problem, answer, null);
            problemList.add(qp);
        }

        // 저장
        QuizSession savedSession = quizSessionRepository.save(quizSession);
        Long sessionId = savedSession.getId();

        return new QuizSessionResponse(sessionId, problemList, quizStartReq.getQuizType());
    }
}
