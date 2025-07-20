package com.jyk.wordquiz.wordquiz.model.entity;

import com.jyk.wordquiz.wordquiz.common.type.QuizType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_session")
@Getter
@Setter
public class QuizSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int score = 0;

    @Column(nullable = false)
    private boolean isQuizActive;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "quiz_type")
    private QuizType quizType = QuizType.MEANING_TO_WORD;

    @Column(name = "attempted_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime attemptedAt;

    @OneToMany(mappedBy = "quizSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizQuestion> quizQuestions = new ArrayList<>();

    public void addQuestion(QuizQuestion question) {
        quizQuestions.add(question);
        question.setQuizSession(this);
    }
}
