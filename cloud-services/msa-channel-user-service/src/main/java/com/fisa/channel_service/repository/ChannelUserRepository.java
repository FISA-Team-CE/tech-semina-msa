package com.fisa.channel_service.repository;

import com.fisa.channel_service.domain.ChannelUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChannelUserRepository extends JpaRepository<ChannelUser, String> {

    // 로그인 ID 중복 체크
    boolean existsByLoginId(String loginId);

    // 로그인 ID로 조회
    Optional<ChannelUser> findByLoginId(String loginId);
}