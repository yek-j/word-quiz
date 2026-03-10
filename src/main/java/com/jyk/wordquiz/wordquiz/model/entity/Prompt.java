package com.jyk.wordquiz.wordquiz.model.entity;

import com.jyk.wordquiz.wordquiz.common.type.PromptType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "prompt")
@Getter
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
}
