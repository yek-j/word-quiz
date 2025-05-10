package com.jyk.wordquiz.wordquiz.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wordbooks")
@Getter
@Setter
public class WordBook {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column
    private String description;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "wordBook", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Word> words = new ArrayList<>();

    @OneToMany(mappedBy = "wordBook")
    private List<QuizWordBook> quizWordBooks = new ArrayList<>();

    // 단어 추가
    public void addWord(Word word) {
        words.add(word);
        word.setWordBook(this);
    }

    // 단어 제거
    public void removeWord(Word word) {
        words.remove(word);
        word.setWordBook(null);
    }

    // 단어장이 퀴즈에서 사용 중인지 확인
    public boolean isUsedInQuizzes() {
        return !this.quizWordBooks.isEmpty();
    }
}
