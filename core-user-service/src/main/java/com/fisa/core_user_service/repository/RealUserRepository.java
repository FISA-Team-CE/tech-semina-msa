package com.fisa.core_user_service.repository;

import com.fisa.core_user_service.domain.RealUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RealUserRepository extends JpaRepository<RealUser, Long> {

    // 이미 가입된 주민번호인지 확인
    boolean existsByResidentNo(String residentNo);

    // username으로 사용자 찾기
    Optional<RealUser> findByUsername(String username);
}