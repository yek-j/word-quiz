package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.exception.ConflictException;
import com.jyk.wordquiz.wordquiz.model.dto.request.QuizAnswerRequest;
import com.jyk.wordquiz.wordquiz.model.dto.response.QuizAnswerResponse;
import com.jyk.wordquiz.wordquiz.model.entity.*;
import com.jyk.wordquiz.wordquiz.repository.QuizRepository;
import com.jyk.wordquiz.wordquiz.repository.QuizSessionRepository;
import com.jyk.wordquiz.wordquiz.repository.QuizTypeRepository;
import com.jyk.wordquiz.wordquiz.repository.UserConnectionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class QuizSessionServiceTest {

    @Mock QuizRepository quizRepository;
    @Mock QuizSessionRepository quizSessionRepository;
    @Mock UserConnectionRepository userConnectionRepository;
    @Mock QuizTypeRepository quizTypeRepository;
    @Mock AIQuestionService aiQuestionService;
    @Mock TransactionTemplate transactionTemplate;

    @InjectMocks QuizSessionService quizSessionService;

    @Test
    @DisplayName("종료된 세션에 답변하면 ConflictException(409)이 발생한다")
    void 종료된_세션에_답변하면_409() {
        // given — 이미 종료된(isQuizActive = false) 세션이 조회되는 상황
        User user = new User();
        user.setId(1L);

        QuizSession endedSession = new QuizSession();
        endedSession.setQuizActive(false);

        given(quizSessionRepository.findByIdAndUser(100L, user))
                .willReturn(Optional.of(endedSession));

        QuizAnswerRequest request = new QuizAnswerRequest();
        request.setWordId(10L);
        request.setAnswer("apple");

        // when & then — 예외 검증은 실행과 검증이 한 덩어리가 된다
        assertThatThrownBy(() -> quizSessionService.getIsCorrect(user, 100L, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("이미 종료된 퀴즈 세션입니다.");
    }

    @Test
    @DisplayName("이미 답변한 문제에 재답변하면 ConflictException(409)이 발생한다")
    void 이미_답변한_문제에_재답변하면_409() {
        // given — 활성 세션에, wordId=10인 문제가 이미 채점된(isCorrect != null) 상태
        User user = new User();
        user.setId(1L);

        Word word = new Word();
        word.setId(10L);

        QuizQuestion answered = new QuizQuestion();
        answered.setWord(word);
        answered.setQuestionOrder(1);
        answered.setIsCorrect(true);          // 이미 답변함

        QuizSession reSession = new QuizSession();
        reSession.setQuizActive(true);        // 세션은 아직 진행 중
        reSession.addQuestion(answered);

        given(quizSessionRepository.findByIdAndUser(100L, user))
                .willReturn(Optional.of(reSession));

        QuizAnswerRequest request = new QuizAnswerRequest();
        request.setWordId(10L);
        request.setAnswer("apple");

        // when & then
        assertThatThrownBy(() -> quizSessionService.getIsCorrect(user, 100L, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("이미 답변한 문제입니다.");
    }

    @Test
    @DisplayName("정답 제출 시 score가 0->1로 변경되고 isCorrect가 true가 된다.")
    void 정답_제출_시() {
        // given — 활성 세션에, wordId=10인 문제가 이미 채점된(isCorrect == null) 상태
        User user = new User();
        user.setId(1L);

        Word word = new Word();
        word.setId(10L);
        word.setTerm("apple");
        word.setDescription("사과");

        QuizQuestion answered = new QuizQuestion();
        answered.setWord(word);
        answered.setQuestionOrder(1);
        answered.setIsCorrect(null);

        QuizType quizType = QuizType.builder().quizTypeName("MEANING_TO_WORD").build();

        QuizSession quizSession = new QuizSession();
        quizSession.setQuizActive(true);
        quizSession.setQuizType(quizType);             // ← 추가
        quizSession.addQuestion(answered);

        given(quizSessionRepository.findByIdAndUser(100L, user))
                .willReturn(Optional.of(quizSession));

        QuizAnswerRequest request = new QuizAnswerRequest();
        request.setWordId(10L);
        request.setAnswer("apple");

        // when & then
        QuizAnswerResponse response = quizSessionService.getIsCorrect(user, 100L, request);

        assertThat(response.isCorrect()).isTrue();
        assertThat(quizSession.getScore()).isEqualTo(1);
    }

}
