package com.jyk.wordquiz.wordquiz.service;

import com.jyk.wordquiz.wordquiz.model.dto.response.*;
import com.jyk.wordquiz.wordquiz.model.entity.*;
import com.jyk.wordquiz.wordquiz.repository.QuizSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalysisService {
    @Autowired
    private QuizSessionRepository quizSessionRepository;

    /**
     * 퀴즈별 통계
     * @param user: 사용자
     * @return
     */
    public QuizAnalysis quizAnalysis(User user) {
        List<QuizSession> sessions = quizSessionRepository.findByUser(user);

        Map<Quiz, List<QuizSession>> quizSessionMap = sessions.stream()
                .collect(Collectors.groupingBy(QuizSession::getQuiz));

        List<QuizStats> quizStatsList = new ArrayList<>();

        for(Map.Entry<Quiz, List<QuizSession>> entry : quizSessionMap.entrySet()) {
            Quiz quiz = entry.getKey();
            List<QuizSession> quizSessions = entry.getValue();

            int attemptCount = quizSessions.size();
            int bestScore = quizSessions.stream().mapToInt(QuizSession::getScore).max().orElse(0);
            double averageScore = quizSessions.stream().mapToInt(QuizSession::getScore).average().orElse(0.0);

            QuizSession lastSession = quizSessions.stream()
                    .max(Comparator.comparing(QuizSession::getAttemptedAt))
                    .orElse(null);

            if (lastSession == null) {
                continue;
            }

            int lastScore = lastSession.getScore();
            LocalDateTime lastAttempted = lastSession.getAttemptedAt();

            QuizStats quizStats = new QuizStats(quiz.getId(),
                    quiz.getName(), attemptCount, bestScore, averageScore,
                    lastScore, lastAttempted);

            quizStatsList.add(quizStats);
        }

        return new QuizAnalysis(quizStatsList, quizStatsList.size());
    }

    /**
     * 단여별 취약점 분석
     * @param user: 사용자
     * @param limit: 최대 개수
     * @param maxAccuracy: 최대 정답률
     * @return WeekWordsAnalysis(단어 정답 횟수, 총 단어 수)
     */
    public WeekWordsAnalysis weekWordsAnalysis(User user, int limit, int maxAccuracy) {
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
     * @param user: 사용자
     * @return LearningOverview(학습한 단어 수, 총 퀴즈 시도 횟수)
     */
    public LearningOverview getLearningOverview(User user) {
        // 총 퀴즈 시도 횟수
        int totalAttempts = quizSessionRepository.countByUser(user);
        // 학습한 단어 수
        int totalWordsLearned = quizSessionRepository.countDistinctWordsByUser(user.getId());

        List<QuizSession> sessions = quizSessionRepository.findByUser(user);

        List<LocalDate> studyDates = sessions.stream()
                .map(session -> session.getAttemptedAt().toLocalDate())
                .distinct()
                .sorted()
                .toList();

        // 연속일 계산
        int consecutiveDays = calculateConsecutiveStudyDays(studyDates);

        // 오늘 공부했는지 확인
        boolean studiedToday = studyDates.contains(LocalDate.now());

        // 이번 주 공부 횟수
        int thisWeekQuizCount = calculateThisWeekQuizCount(sessions);

        // 마지막 공부 날짜
        LocalDate lastStudyDate = null;
        if(!studyDates.isEmpty()) {
            lastStudyDate = studyDates.getLast();
        }

        return new LearningOverview(totalWordsLearned,
                totalAttempts,
                consecutiveDays,
                studiedToday,
                thisWeekQuizCount,
                lastStudyDate);
    }

    /**
     * 연속일 계산 로직 설계
     * @param studyDates: 사용자 퀴즈 시도일 리스트
     * @return 연속일 
     */
    private int calculateConsecutiveStudyDays(List<LocalDate> studyDates) {
        if (studyDates.isEmpty()) {
            return 0;
        }

        LocalDate checkDate = LocalDate.now();
        int consecutiveDays = 0;

        // 오늘 공부 안했을 경우
        if (!studyDates.contains(checkDate)) {
            checkDate = checkDate.minusDays(1);
        }

        while (studyDates.contains(checkDate)) {
            consecutiveDays++;
            checkDate = checkDate.minusDays(1); // 하루씩 뒤로
        }

        return consecutiveDays;
    }

    /**
     * 이번 주 공부 횟수
     * @param sessions: 사용자 퀴즈 세션 리스트
     * @return 이번 주 공부한 날 수
     */
    private int calculateThisWeekQuizCount(List<QuizSession> sessions) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY); // 이번주 월요일
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY); // 이번주 일요일

        return (int) sessions.stream()
                .filter(session -> {
                    LocalDate sessionDate = session.getAttemptedAt().toLocalDate();
                    return !sessionDate.isBefore(startOfWeek) && !sessionDate.isAfter(endOfWeek);
                })
                .count();
    }
}
