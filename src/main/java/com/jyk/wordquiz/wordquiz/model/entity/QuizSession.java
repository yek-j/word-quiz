package com.jyk.wordquiz.wordquiz.model.entity;

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

    @Column(name = "attempted_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime attemptedAt;

    @OneToMany(mappedBy = "quizSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizAnswer> quizAnswers = new ArrayList<>();

    public void addAnswer(QuizAnswer answer) {
        quizAnswers.add(answer);
        answer.setQuizSession(this);
    }
}
