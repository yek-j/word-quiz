package com.jyk.wordquiz.wordquiz.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WordBook> wordBooks = new ArrayList<>();

    // 단어장 추가
    public void addWordBook(WordBook wordBook) {
        wordBooks.add(wordBook);
        wordBook.setCreatedBy(this);
    }

    // 단어장 제거
    public void removeWordBook(WordBook wordBook) {
        wordBooks.remove(wordBook);
        wordBook.setCreatedBy(null);
    }
}
