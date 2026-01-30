package com.fisa.core_user_service.domain;

import com.fisa.core_user_service.converter.EncryptConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.util.UUID;

@Entity
@Table(name = "TB_REAL_USER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RealUser {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID userUuid;

    @Column(nullable = false)
    private String username;

    // DB에는 암호화되어 저장됨 (예: x8F/a9...)
    // 객체로 꺼낼 때는 평문 (예: 홍길동)
    @Convert(converter = EncryptConverter.class)
    @Column(nullable = false)
    private String realName;

    @Convert(converter = EncryptConverter.class)
    @Column(nullable = false, unique = true)
    private String residentNo;

    @Builder
    public RealUser(String username, String realName, String residentNo) {
        this.userUuid = UUID.randomUUID();
        this.username = username;
        this.realName = realName;
        this.residentNo = residentNo;
    }
}