package com.fisa.channel_service.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TB_CHANNEL_USER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChannelUser {

    @Id
    @Column(length = 36)
    private String id; // PK (Channel User ID - UUID String)

    @Column(name = "user_uuid", nullable = false, length = 36)
    private String userUuid; // Core User와 연결되는 UUID

    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(name = "pin_hash", nullable = false, length = 256)
    private String pinHash; // 비밀번호 (PIN) 암호화 값

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }

    @Builder
    public ChannelUser(String loginId, String pinHash, String userUuid) {
        this.loginId = loginId;
        this.pinHash = pinHash;
        this.userUuid = userUuid;
    }

    // 로그인 성공 시 시간 업데이트
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }
}