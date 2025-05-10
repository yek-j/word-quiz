package com.jyk.wordquiz.wordquiz.model.entity;

import com.jyk.wordquiz.wordquiz.common.type.SharingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz")
@Getter
@Setter
public class Quiz {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "sharing_status", columnDefinition = "VARCHAR(20) DEFAULT 'PUBLIC'")
    private SharingStatus sharingStatus = SharingStatus.PUBLIC;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizWordBook> quizWordBooks = new ArrayList<>();

    @OneToMany(mappedBy = "quiz")
    private List<QuizSession> sessions = new ArrayList<>();

    public void addWordBook(WordBook wordBook) {
        QuizWordBook quizWordBook = new QuizWordBook();
        quizWordBook.setQuiz(this);
        quizWordBook.setWordBook(wordBook);
        this.quizWordBooks.add(quizWordBook);
    }

    public void removeWordBook(WordBook wordBook) {
        this.quizWordBooks.removeIf(q -> q.getWordBook().equals(wordBook));
    }
}
