package com.jyk.wordquiz.wordquiz.model.entity;

import com.jyk.wordquiz.wordquiz.common.type.UserConnectionStatus;
import com.jyk.wordquiz.wordquiz.common.type.UserConnectionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_connection",
    uniqueConstraints = @UniqueConstraint(
            columnNames = {"from_user_id", "to_user_id"}
    )
)
@Getter
@Setter
public class UserConnection {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id")
    private User toUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_type", nullable = false)
    private UserConnectionType connectionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_status", nullable = false)
    private UserConnectionStatus connectionStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
