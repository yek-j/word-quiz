package com.jyk.wordquiz.wordquiz.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "quiz_wordbook")
@Getter
@Setter
public class QuizWordBook {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne
    @JoinColumn(name = "wordbook_id", nullable = false)
    private WordBook wordBook;
}
