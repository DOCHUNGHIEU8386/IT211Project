package com.example.it211project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Token string (UUID) */
    @Column(nullable = false, unique = true, length = 512)
    private String token;

    /** Liên kết tới user */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    /** Thời điểm hết hạn */
    @Column(nullable = false)
    private Instant expiryDate;

    /** Đã bị revoke chưa (FR-03 logout) */
    @Builder.Default
    private Boolean revoked = false;
}
