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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class QuizSessionService {
    private final QuizRepository quizRepository;
    private final QuizSessionRepository quizSessionRepository;
    private final UserConnectionRepository userConnectionRepository;
    private final QuizTypeRepository quizTypeRepository;
    private final AIQuestionService aiQuestionService;

    private final TransactionTemplate transactionTemplate;

    private static final String KEY_ANSWER = "answer";
    private static final String KEY_CORRECT = "correct";
    private static final String KEY_ORDER = "order";

    /**
     * startQuiz의 준비 단계(트랜잭션 1) 결과를 저장 단계(트랜잭션 2)로 넘기는 상자.
     * - existing != null : 진행 중인 세션이 이미 있음 → 이 응답을 그대로 반환하면 되고 나머지는 null
     * - existing == null : 새 세션을 만들어야 함 → quiz / quizType / selectedWords 사용
     * 자바 메서드는 값을 하나만 리턴할 수 있어서, 여러 값을 묶어 넘기기 위한 용도다.
     */
    private record Prepared(QuizSessionResponse existing,
                            Quiz quiz,
                            QuizType quizType,
                            List<Word> selectedWords) {}


    public QuizSessionService(QuizRepository quizRepository,
                              QuizSessionRepository quizSessionRepository,
                              UserConnectionRepository userConnectionRepository,
                              QuizTypeRepository quizTypeRepository,
                              AIQuestionService aiQuestionService, TransactionTemplate transactionTemplate) {
        this.quizRepository = quizRepository;
        this.quizSessionRepository = quizSessionRepository;
        this.userConnectionRepository = userConnectionRepository;
        this.quizTypeRepository = quizTypeRepository;
        this.aiQuestionService = aiQuestionService;
        this.transactionTemplate = transactionTemplate;
    }


    /**
     * 지정된 사용자에 대한 퀴즈 세션을 시작합니다: 활성 세션이 존재하면 해당 세션을 반환하고,
     * 그렇지 않으면 지정된 퀴즈와 유형에 따라 최대 20개의 문항으로 구성된 새 세션을 생성하고 저장합니다.
     *
     * [트랜잭션 구조]
     * 이 메서드 자체는 트랜잭션을 열지 않는다(NOT_SUPPORTED). 대신 안에서 짧은 트랜잭션 두 개를 연다.
     *   1) prepare() : DB 읽기 — 퀴즈·타입 조회, 권한 검증, 단어 선정      → 끝나면 커밋, 커넥션 반납
     *   2) callAi()  : Gemini 호출 — 트랜잭션 없음, DB 커넥션을 잡지 않음
     *   3) save()    : DB 쓰기 — 세션·문제 저장                             → 새 트랜잭션
     * 외부 API 응답을 기다리는 수 초 동안 DB 커넥션을 점유하지 않기 위한 분리다.
     *
     * @param user 퀴즈를 시작하는 사용자
     * @param quizStartReq 대상 퀴즈 ID와 원하는 퀴즈 유형이 포함된 데이터를 요청합니다
     * @return 세션 ID, 문제 목록(해당되는 경우 번역 포함) 및 퀴즈 유형을 포함하는 QuizSessionResponse
     * @throws QuizNotFoundException 퀴즈가 존재하지 않거나 사용자가 퀴즈를 시작할 권한이 없는 경우
     */
    // NOT_SUPPORTED: 클래스 레벨 @Transactional(readOnly = true)가 이 메서드에 적용되는 것을 막는다.
    // 이게 없으면 메서드 전체가 readOnly 트랜잭션에 감싸이고, 아래 execute()들은 새 트랜잭션을 열지 못하고
    // 그 readOnly 트랜잭션에 합류해 버린다 → AI 호출은 여전히 트랜잭션 안, save()의 INSERT는 flush 되지 않음.
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public QuizSessionResponse startQuiz(User user, QuizStartRequest quizStartReq) {

        // ── [트랜잭션 1] 준비 ── 이 한 줄이 끝나는 순간 커밋되고 커넥션이 풀로 돌아간다
        Prepared p = transactionTemplate.execute(status -> prepare(user, quizStartReq));

        // 진행 중인 세션이 이미 있으면 그대로 반환. AI 호출도 저장도 필요 없다.
        if (p.existing() != null) {
            return p.existing();
        }

        // ── [트랜잭션 없음] AI 호출 ── 여기서 몇 초가 걸려도 DB 커넥션을 잡고 있지 않다
        // 비-AI 타입이면 null. 아래 람다 안에서 쓰려면 한 번만 대입해야 해서(effectively final) 삼항으로 처리.
        List<QuizProblem> aiProblems = p.quizType().isUseAi() ? callAi(p) : null;

        // ── [트랜잭션 2] 저장 ── 새 트랜잭션을 열어 세션과 문제를 INSERT
        return transactionTemplate.execute(status -> save(user, p, aiProblems));
    }

    /**
     * [트랜잭션 1] 퀴즈 시작에 필요한 것을 DB에서 읽어 모은다.
     *
     * LAZY 컬렉션 접근(getQuizQuestions, getQuizWordBooks, getWords)은 반드시 이 안에서 끝내야 한다.
     * 트랜잭션이 닫힌 뒤(save 단계)에는 준영속 상태라 LAZY 접근 시 LazyInitializationException이 난다.
     *
     * @return 기존 활성 세션이 있으면 existing에 응답을 담고 나머지는 null,
     *         없으면 existing은 null이고 quiz / quizType / selectedWords를 채워서 반환
     */
    private Prepared prepare(User user, QuizStartRequest quizStartReq) {
        // 시작하려는 퀴즈 가져오기
        Quiz quiz = quizRepository.findById(quizStartReq.getQuizId()).orElseThrow(() -> new QuizNotFoundException(quizStartReq.getQuizId()));

        // 기존 세션이 있다면 사용하기
        Optional<QuizSession> activeSession = quizSessionRepository.findByUserAndQuizAndIsQuizActive(user, quiz, true);

        QuizType quizType = quizTypeRepository.findById(quizStartReq.getQuizTypeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 퀴즈 타입입니다. id: " + quizStartReq.getQuizTypeId()));

        // 진행 중인 세션이 있으면 그 문제들을 다시 조립해서 응답으로 만든다 (새로 만들지 않음)
        if(activeSession.isPresent()) {
            List<QuizQuestion> activeQusetions = activeSession.get().getQuizQuestions();
            activeQusetions.sort(Comparator.comparing(QuizQuestion::getQuestionOrder));

            List<QuizProblem> activeProblems = new ArrayList<>();
            for(QuizQuestion q : activeQusetions) {
                Word word = q.getWord();

                String p = word.getDescription();
                String a = word.getTerm();
                String t = "";

                if(activeSession.get().getQuizType().getQuizTypeName().equalsIgnoreCase("WORD_TO_MEANING")) {
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

            // 기존 세션 응답을 existing에 담아 반환 → startQuiz가 이걸 보고 바로 리턴한다
            return new Prepared(
                    new QuizSessionResponse(activeSession.get().getId(), activeProblems, activeSession.get().getQuizType()),
                    null, null, null);
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

        // 퀴즈에서 사용하는 Word 가져오기 — getQuizWordBooks(), getWords() 모두 LAZY라 여기(트랜잭션 1)서 끝내야 함
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

        // 저장 단계에 필요한 것들을 상자에 담아 반환. 여기서 리턴되는 순간 트랜잭션 1이 커밋된다.
        return new Prepared(null, quiz, quizType, selectedWords);
    }

    /**
     * [트랜잭션 없음] AI 문제 생성.
     * startQuiz가 NOT_SUPPORTED이고 두 execute() 블록 사이에서 호출되므로, 이 메서드는 어떤 트랜잭션 안에서도 실행되지 않는다.
     * Gemini 응답을 기다리는 동안 DB 커넥션은 이미 반납된 상태다.
     */
    private List<QuizProblem> callAi(Prepared p) {
        List<QuizProblem> problems = aiQuestionService.generationAiQuestions(p.selectedWords(), p.quizType().getId());

        if (problems == null || problems.isEmpty()) {
            throw new IllegalStateException("AI 퀴즈 문제 생성에 실패했습니다. 프롬프트를 확인해주세요.");
        }
        return problems;
    }

    /**
     * [트랜잭션 2] QuizSession과 QuizQuestion을 조립해 저장한다.
     *
     * p 안의 quiz / quizType / selectedWords는 트랜잭션 1이 끝났으므로 준영속(detached) 상태다.
     * 그래도 setQuiz(), setWord() 같은 @ManyToOne 참조로 꽂는 건 안전하다 — Hibernate는 ID만 FK로 쓴다.
     * 단, 이 안에서 준영속 엔티티의 LAZY 필드를 건드리면 안 된다. 지금은 getId / getTerm / getDescription 같은
     * 이미 로딩된 스칼라 컬럼만 읽는다.
     *
     * @param aiProblems AI 타입이면 callAi() 결과, 비-AI 타입이면 null
     */
    private QuizSessionResponse save(User user, Prepared p, List<QuizProblem> aiProblems) {
        Quiz quiz = p.quiz();
        QuizType quizType = p.quizType();
        List<Word> selectedWords = p.selectedWords();

        List<QuizProblem> problemList = new ArrayList<>();

        // QuizSession 생성
        QuizSession quizSession = new QuizSession();
        quizSession.setQuiz(quiz);          // 준영속 참조 — FK(quiz_id)만 사용됨
        quizSession.setUser(user);
        quizSession.setQuizActive(true);
        quizSession.setQuizType(quizType);  // 준영속 참조 — FK(quiz_type_id)만 사용됨

        int i = 1;

        if (!quizType.isUseAi()) {
            // 비-AI: 선정된 단어로 바로 문제를 만든다
            for (Word w : selectedWords) {
                // QuizQuestion 순서대로 문제 추가
                QuizQuestion question = new QuizQuestion();

                question.setWord(w);        // 준영속 참조 — FK(word_id)만 사용됨
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
            // AI: callAi()가 트랜잭션 밖에서 만들어 온 문제를 단어와 매핑해서 저장한다
            problemList = aiProblems;

            Map<Long, Word> wordById = new HashMap<>();
            for (Word w : selectedWords) {
                wordById.put(w.getId(), w);
            }
            for (QuizProblem qp : problemList) {
                Word w = wordById.get(qp.getWordId());
                if (w == null) continue;
                QuizQuestion question = new QuizQuestion();
                question.setWord(w);
                question.setQuestionOrder(i++);
                question.setIsCorrect(null);
                question.setAiGeneratedSentence(qp.getProblem());
                question.setAiGeneratedTranslation(qp.getTranslation());
                quizSession.addQuestion(question);
            }
        }

        // 저장 — quizQuestions에 cascade = ALL이라 문제들도 함께 INSERT 된다
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

        if (!quizSession.isQuizActive()) {
            throw new IllegalArgumentException("이미 종료된 퀴즈 세션입니다.");
        }

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

        // 마지막 퀴즈 답변 완료 시 퀴즈 세션 종료
        boolean allAnswered =  quizQuestions.stream().allMatch(q -> q.getIsCorrect() != null);

        if (allAnswered) {
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
                if (q.getIsCorrect() != null) {
                    throw new IllegalArgumentException("이미 답변한 문제입니다. wordId="+ quizAnswerReq.getWordId());
                }
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
        if (answerAndCorrect.isEmpty()) {
            throw new IllegalArgumentException("해당 wordId에 대한 문제를 찾을 수 없습니다. wordId=" + quizAnswerReq.getWordId());
        }
        return answerAndCorrect;
    }
}