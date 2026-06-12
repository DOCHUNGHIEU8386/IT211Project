package com.example.it211project.repository;

import com.example.it211project.entity.RefreshToken;
import com.example.it211project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);

    /** FR-03: Xóa tất cả refresh token của user khi logout */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    void deleteByUser(User user);

    /** Kiểm tra token còn valid không (not revoked) */
    boolean existsByTokenAndRevokedFalse(String token);
}
