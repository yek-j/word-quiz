package com.jyk.wordquiz.wordquiz.model.entity;

import com.jyk.wordquiz.wordquiz.common.type.PromptType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "prompt")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prompt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "prompt_type", nullable = false, columnDefinition = "varchar(30)")
    private PromptType promptType;

    @Column(name = "prompt_name", nullable = false, length = 50)
    private String promptName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    private boolean disabled = false;

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
