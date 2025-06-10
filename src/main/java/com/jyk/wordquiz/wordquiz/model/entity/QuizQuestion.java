package com.jyk.wordquiz.wordquiz.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "quiz_question")
@Getter
@Setter
public class QuizQuestion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "quiz_session_id", nullable = false)
    private QuizSession quizSession;

    @ManyToOne
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Column(name = "question_order", nullable = false)
    private int questionOrder;

    @Column(name = "is_correct")
    private Boolean isCorrect;
}
