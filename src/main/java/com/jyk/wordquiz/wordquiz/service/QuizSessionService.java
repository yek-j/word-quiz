package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.exception.QuizNotFoundException;
import com.jyk.wordquiz.wordquiz.common.exception.QuizSessionNotFoundException;
import com.jyk.wordquiz.wordquiz.common.type.QuizType;
import com.jyk.wordquiz.wordquiz.common.type.SharingStatus;
import com.jyk.wordquiz.wordquiz.model.dto.request.QuizAnswerRequest;
import com.jyk.wordquiz.wordquiz.model.dto.request.QuizStartRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizAnswerResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizProblem;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizResultResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizSessionResponse;
import com.jyk.wordquiz.wordquiz.model.entity.*;
import com.jyk.wordquiz.wordquiz.repository.QuizRepository;
import com.jyk.wordquiz.wordquiz.repository.QuizSessionRepository;
import com.jyk.wordquiz.wordquiz.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class QuizSessionService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private QuizSessionRepository quizSessionRepository;

    private static final String KEY_ANSWER = "Answer";
    private static final String KEY_CORRECT = "Correct";
    private static final String KEY_ORDER = "Order";

    /**
     * 퀴즈 시작으로 신규 세션 생성
     * 아직 완료하지 않은 퀴즈면 기존 세션 정보 반환
     * @param user: 사용자
     * @param quizStartReq: 퀴즈 시작을 위한 정보
     * @return 퀴즈 세션 정보
     */
    @Transactional
    public QuizSessionResponse startQuiz(User user, QuizStartRequest quizStartReq) {
        
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
        if(!quiz.getCreatedBy().getId().equals(user.getId())
                && quiz.getSharingStatus() != SharingStatus.PUBLIC) {
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
     * 퀴즈 문제의 답변 채점
     * @param user: 사용자
     * @param sessionId: QuizSession Id
     * @param quizAnswerReq: 사용자 정답
     * @return QuizAnswerResponse
     */
    @Transactional
    public QuizAnswerResponse getIsCorrect(User user, Long sessionId, QuizAnswerRequest quizAnswerReq) {
        
        // 퀴즈 세션 가져오기
        QuizSession quizSession = quizSessionRepository.findByIdAndUser(sessionId, user).orElseThrow(() -> new QuizSessionNotFoundException(sessionId));

        QuizAnswerResponse quizAnswerResponse = new QuizAnswerResponse();
        quizAnswerResponse.setWordId(quizAnswerReq.getWordId());

        // 퀴즈 타입 확인 후 정답 확인
        List<QuizQuestion> quizQuestions = quizSession.getQuizQuestions();
        HashMap<String, Object> result = validateAndSaveAnswer(quizQuestions, quizAnswerReq, quizSession.getQuizType());

        // 정답이면 score +1
        boolean correct = Boolean.parseBoolean(result.get(KEY_CORRECT).toString());
        if (correct) {
            quizSession.setScore(quizSession.getScore() + 1);
        }

        quizAnswerResponse.setCorrectAnswer(result.get(KEY_ANSWER).toString());
        quizAnswerResponse.setCorrect(correct);

        // 현재 답변한 문제의 순서 확인
        int currentOrder = Integer.parseInt(result.get(KEY_ORDER).toString());
        int quizQuestionSize = quizQuestions.size();

        // 마지막 퀴즈 답변 완료 시 퀴즈 세션 종료
        if(currentOrder == quizQuestionSize) {
            quizSession.setQuizActive(false);
        }

        // 퀴즈 푼 시간
        quizSession.setAttemptedAt(LocalDateTime.now());

        return quizAnswerResponse;
    }

    /**
     * 퀴즈 결과 반환
     * @param user: 사용자
     * @param sessionId: quiz session id
     * @return : QuizResultResponse
     */
    public QuizResultResponse getQuizResult(User user, Long sessionId) {
        
        // 퀴즈 세션 가져오기
        QuizSession quizSession = quizSessionRepository.findByIdAndUser(sessionId, user).orElseThrow(() -> new QuizSessionNotFoundException(sessionId));

        // isCorrect가 null이 아닌 문항 찾기
        List<QuizQuestion> quizQuestions = quizSession.getQuizQuestions();
        long answeredCount = quizQuestions.stream()
                .filter(q -> q.getIsCorrect() != null)
                .count();

        QuizResultResponse quizResult = new QuizResultResponse(quizSession.getScore(), answeredCount, quizSession.getAttemptedAt());

        return quizResult;
    }

    /**
     * 퀴즈 정답 셋팅
     * @param quizQuestions: 퀴즈 문제들
     * @param quizAnswerReq: 답변
     * @param quizType: 현재 퀴즈 타입
     * @return HashMap<String, Object>
     */
    private HashMap<String, Object> validateAndSaveAnswer(List<QuizQuestion> quizQuestions, QuizAnswerRequest quizAnswerReq, QuizType quizType) {
        boolean isCorrect = false;
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
                answerAndCorrect.put(KEY_ORDER, q.getQuestionOrder());
            }
        }
        return answerAndCorrect;
    }
}
