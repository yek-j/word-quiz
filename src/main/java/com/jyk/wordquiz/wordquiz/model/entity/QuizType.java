package com.jyk.wordquiz.wordquiz.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_type")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizType {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quiz_type_name", nullable = false, unique = true, columnDefinition = "varchar(50)")
    private String quizTypeName;

    @Column(name = "quiz_type_description", nullable = false, columnDefinition = "varchar(500)")
    private String quizTypeDescription;

    @Column(name = "use_ai", nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    private boolean useAi = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false)
    private Long createdBy ;

    @Column(name = "last_modified_by", nullable = false)
    private Long lastModifiedBy;
}
