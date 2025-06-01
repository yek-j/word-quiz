package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.auth.JwtTokenProvider;
import com.jyk.wordquiz.wordquiz.common.exception.AuthenticatedUserNotFoundException;
import com.jyk.wordquiz.wordquiz.common.type.SharingStatus;
import com.jyk.wordquiz.wordquiz.model.dto.request.CreateQuizRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.Quizzes;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizzesResponse;
import com.jyk.wordquiz.wordquiz.model.entity.Quiz;
import com.jyk.wordquiz.wordquiz.model.entity.QuizSession;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.model.entity.WordBook;
import com.jyk.wordquiz.wordquiz.repository.QuizRepository;
import com.jyk.wordquiz.wordquiz.repository.QuizSessionRepository;
import com.jyk.wordquiz.wordquiz.repository.UserRepository;
import com.jyk.wordquiz.wordquiz.repository.WordBookRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizService {
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WordBookRepository wordBookRepository;
    @Autowired
    private QuizSessionRepository quizSessionRepository;
    @Autowired
    private JwtTokenProvider provider;

    /**
     * 퀴즈 생성
     * @param token: jwt token
     * @param createQuizRequest: 퀴즈 생성을 위한 이름, 설명, 단어장 ID 들, 퀴즈 공개 여부
     */
    @Transactional
    public void createQuiz(String token, CreateQuizRequest createQuizRequest) {
        List<Long> wordBookIds = parseAndValidateWordBookIds(createQuizRequest.getWordBookIds());
        Long userId = provider.getSubject(token);
        User user =  userRepository.findById(userId).orElseThrow(() -> new AuthenticatedUserNotFoundException(userId));

        Quiz newQuiz = new Quiz();
        newQuiz.setName(createQuizRequest.getName());
        newQuiz.setDescription(createQuizRequest.getDescription());
        newQuiz.setSharingStatus(createQuizRequest.getSharingStatus());
        newQuiz.setCreatedBy(user);

        int wordBookSize = 0;
        if(!wordBookIds.isEmpty()) {
            for(Long id : wordBookIds) {
                Optional<WordBook> wordBook = wordBookRepository.findById(id);

                if(wordBook.isPresent()) {
                    newQuiz.addWordBook(wordBook.get());
                    wordBookSize++;
                }
            }
        } else {
            throw new IllegalArgumentException("퀴즈 생성을 위해 단어장이 필요합니다.");
        }

        if(wordBookSize != wordBookIds.size()) {
            throw new EntityNotFoundException("선택한 단어장을 찾을 수 없습니다. 유효한 단어장을 선택해주세요.");
        }

        quizRepository.save(newQuiz);

    }

    /**
     * 퀴즈 리스트 가져오기
     * TODO: 세션 생성 후 테스트
     * @param token: jwt token
     * @param page: page 값
     * @param criteria: orderby
     * @param sort: DESC, ASC
     * @param kind: 공유된 모든 퀴즈보기(ALL), 자신의 퀴즈만 보기(MY)
     * @return : QuizzesResponse
     */
    public QuizzesResponse getQuizList(String token, int page, String criteria, String sort, String kind) {
        Long userId = provider.getSubject(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new AuthenticatedUserNotFoundException(userId));

        Sort.Direction direction = Sort.Direction.DESC;

        if(sort.equals("ASC")) {
            direction = Sort.Direction.ASC;
        }

        Pageable pageReq = PageRequest.of(page, 10, Sort.by(direction, criteria));

        Page<Quiz> findQuizzes = null;

        if(kind.equals("MY")) findQuizzes = quizRepository.findByUser(user, pageReq);
        else if(kind.equals("ALL")) findQuizzes = quizRepository.findBySharingStatusOrMy(SharingStatus.PUBLIC, user, pageReq);
        // TODO: FRIENDS

        Page<Quizzes> pageQuizzes = Objects.requireNonNull(findQuizzes).map(q -> new Quizzes(
                q.getId(), q.getName(), q.getDescription(), q.getCreatedBy().getUsername(), q.getCreatedAt()
        ));

        int totalPages = pageQuizzes.getTotalPages();
        List<Quizzes> quizzes = pageQuizzes.getContent();

        // QuizSession 확인
        List<Long> quizIds = new ArrayList<>();
        for(Quizzes quiz : quizzes) {
            quizIds.add(quiz.getId());
        }

        if(!quizIds.isEmpty()) {
            List<QuizSession> quizSessions = quizSessionRepository.findLatestSessionsByUserAndQuizIds(userId, quizIds);

            Map<Long, Boolean> activeStatusMap = quizSessions.stream()
                    .collect(Collectors.toMap(
                            session -> session.getQuiz().getId(),
                            QuizSession::isQuizActive
                    ));

            for(Quizzes quiz : quizzes) {
                quiz.setQuizActive(activeStatusMap.getOrDefault(quiz.getId(), false));
            }
        }

        return new QuizzesResponse(quizzes, totalPages);
    }

    /**
     * WordBook Id 리스트 검증
     * @param strIds: 문자열 wordBookId 리스트 (123,124,567)
     * @return 전달받은 값 중 유효하면서 중복이 제거된 wordBookId 리스트
     */
    private List<Long> parseAndValidateWordBookIds(String strIds) {
        // null 또는 빈 문자열 체크
        if(strIds.isBlank()) {
            throw new IllegalArgumentException("퀴즈 생성을 위해 단어장이 필요합니다.");
        }

        // 파싱 및 변환
        Set<Long> uniqueIds = new LinkedHashSet<>(); // 순서 유지하면서 중복 제거
        String[] arrIds = strIds.split(",");

        for(String strId : arrIds) {
            String trimId = strId.trim();
            if(trimId.isEmpty()) continue;

            try {
                long id = Long.parseLong(trimId);

                if(id <= 0) {
                    throw new IllegalArgumentException("유효하지 않은 ID입니다: " + id);
                }

                uniqueIds.add(id);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "숫자가 아닌 ID가 포함되어 있습니다: " + trimId
                );
            }
        }

        if(uniqueIds.isEmpty()) {
            throw new IllegalArgumentException("퀴즈 생성을 위해 단어장이 필요합니다.");
        }

        return new ArrayList<>(uniqueIds);
    }
}
