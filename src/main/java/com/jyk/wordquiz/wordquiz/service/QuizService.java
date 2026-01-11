package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.exception.QuizNotFoundException;
import com.jyk.wordquiz.wordquiz.model.dto.request.QuizParamRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.Quizzes;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizzesResponse;
import com.jyk.wordquiz.wordquiz.model.dto.response.WordBooks;
import com.jyk.wordquiz.wordquiz.model.entity.Quiz;
import com.jyk.wordquiz.wordquiz.model.entity.QuizSession;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.model.entity.WordBook;
import com.jyk.wordquiz.wordquiz.repository.QuizRepository;
import com.jyk.wordquiz.wordquiz.repository.QuizSessionRepository;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QuizService {
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private WordBookRepository wordBookRepository;
    @Autowired
    private QuizSessionRepository quizSessionRepository;

    /**
     * 퀴즈 생성
     * @param user: 사용자
     * @param quizParamRequest: 퀴즈 생성을 위한 이름, 설명, 단어장 ID 들, 퀴즈 공개 여부
     */
    @Transactional
    public void createQuiz(User user, QuizParamRequest quizParamRequest) {
        List<Long> wordBookIds = parseAndValidateWordBookIds(quizParamRequest.getWordBookIds());
        
        Quiz newQuiz = new Quiz();
        newQuiz.setName(quizParamRequest.getName());
        newQuiz.setDescription(quizParamRequest.getDescription());
        newQuiz.setSharingStatus(quizParamRequest.getSharingStatus());
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

    public QuizResponse getQuiz(User user, Long quizId) {
        Quiz quiz = quizRepository.findByCreatedByAndId(user, quizId).orElseThrow(() -> new QuizNotFoundException(quizId));;

        List<WordBooks> wordBooks = quiz.getQuizWordBooks().stream().map(WordBooks::new).toList();

        return new QuizResponse(quiz.getName(), quiz.getCreatedBy().getUsername(), quiz.getSharingStatus(), quiz.getDescription(), wordBooks);
    }

    /**
     * 퀴즈 리스트 가져오기
     *
     * @param user     : 사용자
     * @param page     : page 값
     * @param criteria : orderby
     * @param sort     : DESC, ASC
     * @param kind     : 볼 수 있는 모든 퀴즈보기(ALL), 자신의 퀴즈만 보기(MY), 친구만 보기(FRIENDS)
     * @param searchId : 검색하려는 사용자 ID
     * @return : QuizzesResponse
     */
    public QuizzesResponse getQuizList(User user, int page, String criteria, String sort, String kind, Long searchId) {
        Sort.Direction direction = Sort.Direction.DESC;

        if(sort.equals("ASC")) {
            direction = Sort.Direction.ASC;
        }

        Pageable pageReq = PageRequest.of(page, 10, Sort.by(direction, criteria));

        Page<Quiz> findQuizzes = null;

        if (kind.equals("MY")) {
            if (searchId != null && Objects.equals(user.getId(), searchId)) findQuizzes = quizRepository.findByCreatedBy(user, pageReq);
        } else {
            if(searchId != null) {
                findQuizzes = quizRepository.findSearchIdQuizzes(user, searchId, pageReq);
            } else {
                if (kind.equals("FRIENDS")) findQuizzes = quizRepository.findByFriendQuizzes(user, pageReq);
                else findQuizzes = quizRepository.findAccessibleQuizzes(user, pageReq);
            }
        }

        if(findQuizzes == null) {
            return new QuizzesResponse(null, 0);
        }

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
            List<QuizSession> quizSessions = quizSessionRepository.findLatestSessionsByUserAndQuizIds(user.getId(), quizIds);

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
     * 퀴즈 수정
     * @param user: 사용자
     * @param qid: 퀴즈 ID
     * @param quizParamRequest: 퀴즈 수정을 위한 파라미터
     */
    @Transactional
    public void updateQuiz(User user, Long qid, QuizParamRequest quizParamRequest) {
        List<Long> wordBookIds = parseAndValidateWordBookIds(quizParamRequest.getWordBookIds());
        
        
        // 사용자 본인의 퀴즈만 수정이 가능하다.
        Quiz quiz = quizRepository.findByCreatedByAndId(user, qid).orElseThrow(() -> new QuizNotFoundException(qid));

        quiz.setName(quizParamRequest.getName());
        quiz.setDescription(quizParamRequest.getDescription());
        quiz.setSharingStatus(quizParamRequest.getSharingStatus());

        int wordBookSize = 0;
        if(!wordBookIds.isEmpty()) {
            for(Long id : wordBookIds) {
                Optional<WordBook> wordBook = wordBookRepository.findById(id);

                if(wordBook.isPresent()) {
                    quiz.addWordBook(wordBook.get());
                    wordBookSize++;
                }
            }
        } else {
            throw new IllegalArgumentException("퀴즈 수정을 위해 단어장이 필요합니다.");
        }

        if(wordBookSize != wordBookIds.size()) {
            throw new EntityNotFoundException("선택한 단어장을 찾을 수 없습니다. 유효한 단어장을 선택해주세요.");
        }

        quizRepository.save(quiz);
    }

    /**
     * 퀴즈 삭제
     * @param user: 사용자
     * @param qid: 퀴즈 ID
     */
    @Transactional
    public void deleteQuiz(User user, Long qid) {
        Quiz quiz = quizRepository.findByCreatedByAndId(user, qid).orElseThrow(() -> new QuizNotFoundException(qid));

        quizRepository.delete(quiz);
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
