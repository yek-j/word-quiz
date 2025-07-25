package com.jyk.wordquiz.wordquiz.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "words")
@Getter
@Setter
public class Word {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "wordbook_id", nullable = false)
    private WordBook wordBook;

    @Column(nullable = false, length = 100)
    private String term;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "word")
    private List<QuizQuestion> quizQuestions = new ArrayList<>();

}
