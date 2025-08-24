package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.common.auth.JwtTokenProvider;
import com.jyk.wordquiz.wordquiz.common.exception.AuthenticatedUserNotFoundException;
import com.jyk.wordquiz.wordquiz.model.dto.response.LearningOverview;
import com.jyk.wordquiz.wordquiz.model.dto.response.WeekWordStats;
import com.jyk.wordquiz.wordquiz.model.dto.response.WeekWordsAnalysis;
import com.jyk.wordquiz.wordquiz.model.entity.QuizQuestion;
import com.jyk.wordquiz.wordquiz.model.entity.QuizSession;
import com.jyk.wordquiz.wordquiz.model.entity.User;
import com.jyk.wordquiz.wordquiz.model.entity.Word;
import com.jyk.wordquiz.wordquiz.repository.QuizSessionRepository;
import com.jyk.wordquiz.wordquiz.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AnalysisService {
    @Autowired
    private JwtTokenProvider provider;
    @Autowired
    private QuizSessionRepository quizSessionRepository;
    @Autowired
    private UserRepository userRepository;

    /**
     * 단여별 취약점 분석
     * @param token: jwt 토큰
     * @param limit: 최대 개수
     * @param maxAccuracy: 최대 정답률
     * @return WeekWordsAnalysis(단어 정답 횟수, 총 단어 수)
     */
    public WeekWordsAnalysis weekWordsAnalysis(String token, int limit, int maxAccuracy) {
        Long userId = provider.getSubject(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new AuthenticatedUserNotFoundException(userId));

        List<QuizSession> sessions = quizSessionRepository.findByUserAndIsQuizActive(user, false);

        List<WeekWordStats> weekWordsList = new ArrayList<>();
        Map<Word, List<Boolean>> wordResultMap = new HashMap<>(); // 정오답 집계용
        Map<Word, LocalDateTime> lastAttemptedMap = new HashMap<>(); // 퀴즈 마지막 날짜 집계용

        // 단어별 정답/오답 수집
        for(QuizSession session : sessions) {
            for(QuizQuestion question : session.getQuizQuestions()) {
                Word word = question.getWord();
                wordResultMap.computeIfAbsent(word, k -> new ArrayList<>())
                        .add(question.getIsCorrect());

                lastAttemptedMap.merge(word, session.getAttemptedAt(),
                        (existing, newTime) -> newTime.isAfter(existing) ? newTime : existing);
            }
        }

        // WeekWordStats 객체로 변환
        for(Map.Entry<Word, List<Boolean>> entry : wordResultMap.entrySet()) {
            Word word = entry.getKey();
            List<Boolean> results = entry.getValue();

            int totalAttempts = results.size();
            long wrongAttempts = results.stream().filter(r -> !r).count();
            double accuracyRate = (totalAttempts - wrongAttempts) * 100.0 / totalAttempts;

            //취약 단어만 (정답률 기준)
            if(accuracyRate <= maxAccuracy) {
                weekWordsList.add(new WeekWordStats(
                   word.getId(),
                   word.getTerm(),
                   word.getWordBook().getName(),
                   totalAttempts,
                   (int) wrongAttempts,
                   totalAttempts - (int) wrongAttempts,
                   accuracyRate,
                   lastAttemptedMap.get(word)
                ));
            }
        }

        List<WeekWordStats> finalList = weekWordsList.stream()
                .sorted(Comparator.comparing(WeekWordStats::getAccuracyRate)) // 정답률 낮은순
                .limit(limit)
                .toList();

        return new WeekWordsAnalysis(finalList, wordResultMap.size());
    }

    /**
     * 전체 학습 통계
     * @param token: jwt 토큰
     * @return LearningOverview(학습한 단어 수, 총 퀴즈 시도 횟수)
     */
    public LearningOverview getLearningOverview(String token) {
        Long userId = provider.getSubject(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new AuthenticatedUserNotFoundException(userId));

        // 총 퀴즈 시도 횟수
        int totalAttempts = quizSessionRepository.countByUser(user);
        // 학습한 단어 수
        int totalWordsLearned = quizSessionRepository.countDistinctWordsByUser(userId);

        return new LearningOverview(totalWordsLearned, totalAttempts);
    }
}
