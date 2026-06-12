package com.example.it211project.service;

/**
 * FR-13 (AF3): Service quản lý Token Blacklist sử dụng Redis
 * Thay thế việc lưu token trong database
 */
public interface TokenBlacklistService {

    /**
     * Thêm token vào blacklist với thời gian sống bằng TTL của token
     * @param token JWT token cần blacklist
     * @param ttlSeconds thời gian sống của token (giây)
     */
    void blacklistToken(String token, long ttlSeconds);

    /**
     * Kiểm tra token có nằm trong blacklist không
     * @param token JWT token cần kiểm tra
     * @return true nếu token đã bị blacklist, false nếu chưa
     */
    boolean isTokenBlacklisted(String token);

    /**
     * Xóa token khỏi blacklist (khi token hết hạn tự nhiên)
     * @param token JWT token cần xóa
     */
    void removeFromBlacklist(String token);
}