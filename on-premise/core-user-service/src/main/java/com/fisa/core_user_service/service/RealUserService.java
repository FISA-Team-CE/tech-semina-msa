package com.fisa.core_user_service.service;

import com.fisa.core_user_service.domain.RealUser;
import com.fisa.core_user_service.repository.RealUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RealUserService {

    private final RealUserRepository realUserRepository;

    // 회원 등록
    @Transactional
    public RealUser registerUser(String username, String realName, String residentNo) {
        // 1. 중복 가입 체크
        if (realUserRepository.existsByResidentNo(residentNo)) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }

        // 2. 저장
        RealUser newUser = RealUser.builder()
                .username(username)
                .realName(realName)
                .residentNo(residentNo)
                .build();

        return realUserRepository.save(newUser);
    }
}