package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.auth.JwtTokenProvider;
import com.jyk.wordquiz.wordquiz.common.exception.AuthenticatedUserNotFoundException;
import com.jyk.wordquiz.wordquiz.common.exception.QuizNotFoundException;
import com.jyk.wordquiz.wordquiz.common.exception.QuizSessionNotFoundException;
import com.jyk.wordquiz.wordquiz.common.type.QuizType;
import com.jyk.wordquiz.wordquiz.common.type.SharingStatus;
import com.jyk.wordquiz.wordquiz.model.dto.request.QuizAnswerRequest;
import com.jyk.wordquiz.wordquiz.model.dto.request.QuizStartRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizAnswerResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizProblem;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizSessionResponse;
import com.jyk.wordquiz.wordquiz.model.entity.*;
import com.jyk.wordquiz.wordquiz.repository.QuizRepository;
import com.jyk.wordquiz.wordquiz.repository.QuizSessionRepository;
import com.jyk.wordquiz.wordquiz.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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

    private static final String KEY_ANSWER = "Answer";
    private static final String KEY_CORRECT = "Correct";

    /**
     * 퀴즈 시작으로 신규 세션 생성
     * 아직 완료하지 않은 퀴즈면 기존 세션 정보 반환
     * @param token: jwt 토큰
     * @param quizStartReq: 퀴즈 시작을 위한 정보
     * @return 퀴즈 세션 정보
     */
    @Transactional
    public QuizSessionResponse startQuiz(String token, QuizStartRequest quizStartReq) {
        Long userId = provider.getSubject(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new AuthenticatedUserNotFoundException(userId));

        // 시작하려는 퀴즈 가져오기
        Quiz quiz = quizRepository.findById(quizStartReq.getQuizId()).orElseThrow(() -> new QuizNotFoundException(quizStartReq.getQuizId()));

        // 기존 세션이 있다면 사용하기
        Optional<QuizSession> activeSession = quizSessionRepository.findByUserAndQuizAndIsQuizActive(user, quiz, true);

        if(activeSession.isPresent()) {
            List<QuizQuestion> activeQusetions = activeSession.get().getQuizQuestions();
            activeQusetions.sort(Comparator.comparing(QuizQuestion::getQuestionOrder));

            List<QuizProblem> activeProblems = new ArrayList<>();
            for(QuizQuestion q : activeQusetions) {
                Word word = q.getWord();

                String p = word.getDescription();
                String a = word.getTerm();

                if(quizStartReq.getQuizType() == QuizType.WORD_TO_MEANING) {
                    p = word.getTerm();
                    a = word.getDescription();
                }

                // 채점되지 않은 답은 보여주지 않는다.
                String displayAnswer = (q.getIsCorrect() != null) ? a : null;
                activeProblems.add(new QuizProblem(word.getId(), p, displayAnswer, q.getIsCorrect()));
            }

            return new QuizSessionResponse(activeSession.get().getId(), activeProblems, activeSession.get().getQuizType());
        }

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
            String answer = null;

            if(quizStartReq.getQuizType() == QuizType.WORD_TO_MEANING) {
                problem = w.getTerm();
                answer = null;
            }

            QuizProblem qp = new QuizProblem(w.getId(), problem, answer, null);
            problemList.add(qp);
        }

        // 저장
        QuizSession savedSession = quizSessionRepository.save(quizSession);
        Long sessionId = savedSession.getId();

        return new QuizSessionResponse(sessionId, problemList, quizStartReq.getQuizType());
    }

    /**
     * TODO: 답변 제출 후 정답에 대한 결과를 DB에 저장하고 결과를 반환한다.
     * @param token: jwt token
     * @param sessionId: QuizSession Id
     * @param quizAnswerReq: 사용자 정답
     * @return QuizAnswerResponse
     */
    @Transactional
    public QuizAnswerResponse getIsCorrect(String token, Long sessionId, QuizAnswerRequest quizAnswerReq) {
        Long userId = provider.getSubject(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new AuthenticatedUserNotFoundException(userId));

        // 퀴즈 세션 가져오기
        QuizSession quizSession = quizSessionRepository.findByIdAndUser(sessionId, user).orElseThrow(() -> new QuizSessionNotFoundException(sessionId));

        QuizAnswerResponse quizAnswerResponse = new QuizAnswerResponse();
        quizAnswerResponse.setWordId(quizAnswerReq.getWordId());

        // 퀴즈 타입 확인 후 정답 확인
        List<QuizQuestion> quizQuestions = quizSession.getQuizQuestions();
        HashMap<String, Object> result = validateAndSaveAnswer(quizQuestions, quizAnswerReq, quizSession.getQuizType());

        quizAnswerResponse.setCorrectAnswer(result.get(KEY_ANSWER).toString());
        quizAnswerResponse.setCorrect(Boolean.parseBoolean(result.get(KEY_CORRECT ).toString()));

        return quizAnswerResponse;
    }

    private HashMap<String, Object> validateAndSaveAnswer(List<QuizQuestion> quizQuestions, QuizAnswerRequest quizAnswerReq, QuizType quizType) {
        Boolean isCorrect = null;
        HashMap<String, Object> answerAndCorrect = new HashMap<>();

        for (QuizQuestion q : quizQuestions) {
            if (Objects.equals(q.getWord().getId(), quizAnswerReq.getWordId())) {
                if (quizType == QuizType.WORD_TO_MEANING) {
                    isCorrect = Objects.equals(q.getWord().getDescription(), quizAnswerReq.getAnswer());
                    answerAndCorrect.put(KEY_ANSWER, q.getWord().getDescription());
                } else {
                    isCorrect = Objects.equals(q.getWord().getTerm(), quizAnswerReq.getAnswer());
                    answerAndCorrect.put(KEY_ANSWER, q.getWord().getTerm());
                }
                q.setIsCorrect(isCorrect);
                answerAndCorrect.put(KEY_CORRECT , isCorrect);
            }
        }
        return answerAndCorrect;
    }
}
