package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.exception.QuizNotFoundException;
import com.jyk.wordquiz.wordquiz.common.exception.QuizSessionNotFoundException;
import com.jyk.wordquiz.wordquiz.common.type.SharingStatus;
import com.jyk.wordquiz.wordquiz.common.type.UserConnectionStatus;
import com.jyk.wordquiz.wordquiz.common.type.UserConnectionType;
import com.jyk.wordquiz.wordquiz.model.dto.request.QuizAnswerRequest;
import com.jyk.wordquiz.wordquiz.model.dto.request.QuizStartRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.*;
import com.jyk.wordquiz.wordquiz.model.entity.*;
import com.jyk.wordquiz.wordquiz.repository.QuizRepository;
import com.jyk.wordquiz.wordquiz.repository.QuizSessionRepository;
import com.jyk.wordquiz.wordquiz.repository.QuizTypeRepository;
import com.jyk.wordquiz.wordquiz.repository.UserConnectionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class QuizSessionService {
    private final QuizRepository quizRepository;
    private final QuizSessionRepository quizSessionRepository;
    private final UserConnectionRepository userConnectionRepository;
    private final QuizTypeRepository quizTypeRepository;
    private final AIQuestionService aiQuestionService;


    public QuizSessionService(QuizRepository quizRepository,
                              QuizSessionRepository quizSessionRepository,
                              UserConnectionRepository userConnectionRepository,
                              QuizTypeRepository quizTypeRepository,
                              AIQuestionService aiQuestionService) {
        this.quizRepository = quizRepository;
        this.quizSessionRepository = quizSessionRepository;
        this.userConnectionRepository = userConnectionRepository;
        this.quizTypeRepository = quizTypeRepository;
        this.aiQuestionService = aiQuestionService;
    }


    private static final String KEY_ANSWER = "Answer";
    private static final String KEY_CORRECT = "Correct";
    private static final String KEY_ORDER = "Order";

    /**
     * 지정된 사용자에 대한 퀴즈 세션을 시작합니다: 활성 세션이 존재하면 해당 세션을 반환하고,
     * 그렇지 않으면 지정된 퀴즈와 유형에 따라 최대 20개의 문항으로 구성된 새 세션을 생성하고 저장합니다.
     *
     * @param user 퀴즈를 시작하는 사용자
     * @param quizStartReq 대상 퀴즈 ID와 원하는 퀴즈 유형이 포함된 데이터를 요청합니다
     * @return 세션 ID, 문제 목록(해당되는 경우 번역 포함) 및 퀴즈 유형을 포함하는 QuizSessionResponse
     * @throws QuizNotFoundException 퀴즈가 존재하지 않거나 사용자가 퀴즈를 시작할 권한이 없는 경우
     */
    @Transactional
    public QuizSessionResponse startQuiz(User user, QuizStartRequest quizStartReq) {
        
        // 시작하려는 퀴즈 가져오기
        Quiz quiz = quizRepository.findById(quizStartReq.getQuizId()).orElseThrow(() -> new QuizNotFoundException(quizStartReq.getQuizId()));

        // 기존 세션이 있다면 사용하기
        Optional<QuizSession> activeSession = quizSessionRepository.findByUserAndQuizAndIsQuizActive(user, quiz, true);

        QuizType quizType = quizTypeRepository.findById(quizStartReq.getQuizTypeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 퀴즈 타입입니다. id: " + quizStartReq.getQuizTypeId()));

        // QuizTypeId를 찾을 수 없다면 WORD_TO_MEANING, MEANING_TO_WORD만 지원한다.
        if(activeSession.isPresent()) {
            List<QuizQuestion> activeQusetions = activeSession.get().getQuizQuestions();
            activeQusetions.sort(Comparator.comparing(QuizQuestion::getQuestionOrder));

            List<QuizProblem> activeProblems = new ArrayList<>();
            for(QuizQuestion q : activeQusetions) {
                Word word = q.getWord();

                String p = word.getDescription();
                String a = word.getTerm();
                String t = "";

                if(quizType.getQuizTypeName().equalsIgnoreCase("WORD_TO_MEANING")) {
                    p = word.getTerm();
                    a = word.getDescription();
                } else if(quizType.isUseAi()) {
                    p = q.getAiGeneratedSentence();
                    t = q.getAiGeneratedTranslation();
                }

                // 채점되지 않은 답은 보여주지 않는다.
                String displayAnswer = (q.getIsCorrect() != null) ? a : null;
                activeProblems.add(new QuizProblem(word.getId(), p, displayAnswer, t, q.getIsCorrect()));
            }

            return new QuizSessionResponse(activeSession.get().getId(), activeProblems, activeSession.get().getQuizType());
        }

        // 본인이 만들었거나 PUBLIC 퀴즈인지 확인
        if(!quiz.getCreatedBy().getId().equals(user.getId()) && quiz.getSharingStatus() != SharingStatus.PUBLIC) {
            // 본인이 아닌 PRIVATE 퀴즈는 접근 불가
            if(quiz.getSharingStatus() == SharingStatus.PRIVATE) {
                throw new QuizNotFoundException(quiz.getId());
            }
            // 친구의 아닌 경우 제외
            Optional<UserConnection> userConnection = Optional.ofNullable(userConnectionRepository.findByUserAndTargetUserAndConnectionTypeAndConnectionStatus(
                    user, quiz.getCreatedBy(), UserConnectionType.FRIEND, UserConnectionStatus.ACCEPTED
            ).orElseThrow(() -> new QuizNotFoundException(quiz.getId())));
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
        quizSession.setQuizType(quizType);

        int i = 1;

        if (!quizType.isUseAi()) {
            for (Word w : selectedWords) {
                // QuizQuestion 순서대로 문제 추가
                QuizQuestion question = new QuizQuestion();

                question.setWord(w);
                question.setQuestionOrder(i++);
                question.setIsCorrect(null);

                String problem = w.getDescription();
                String answer = null;
                String translation = null;

                if (quizType.getQuizTypeName().equalsIgnoreCase("WORD_TO_MEANING")) {
                    problem = w.getTerm();
                }

                quizSession.addQuestion(question);

                QuizProblem qp = new QuizProblem(w.getId(), problem, answer, translation, null);
                problemList.add(qp);
            }
        } else {
            problemList = aiQuestionService.generationAiQuestions(selectedWords, quizType.getId());
        }

        // 저장
        QuizSession savedSession = quizSessionRepository.save(quizSession);
        Long sessionId = savedSession.getId();

        return new QuizSessionResponse(sessionId, problemList, quizType);
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
                if (quizType.getQuizTypeName().equalsIgnoreCase("WORD_TO_MEANING")) {
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