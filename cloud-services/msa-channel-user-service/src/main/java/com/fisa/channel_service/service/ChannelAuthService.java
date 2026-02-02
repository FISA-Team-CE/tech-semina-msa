package com.fisa.channel_service.service;

import com.fisa.channel_service.client.CoreUserClient;
import com.fisa.channel_service.domain.ChannelUser;
import com.fisa.channel_service.dto.user.CoreUserRequestDto;
import com.fisa.channel_service.dto.user.SignUpRequestDto;
import com.fisa.channel_service.repository.ChannelUserRepository;
import com.fisa.channel_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChannelAuthService {

    private final ChannelUserRepository userRepository;
    private final CoreUserClient coreUserClient; // Feign Client
    private final PasswordEncoder passwordEncoder; // Spring Security
    private final JwtUtil jwtUtil;

    @Transactional
    public void signUp(SignUpRequestDto request) {
        // 1. (Channel) 아이디 중복 체크
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new IllegalArgumentException("이미 사용 중인 ID입니다.");
        }

        // 2. (Core) 실명 정보 등록 요청 -> UUID 발급받음 (VPN 통신)
        var coreResponse = coreUserClient.registerUser(new CoreUserRequestDto(
                request.loginId(),
                request.realName(),
                request.residentNo()
        ));

        // 3. (Channel) UUID + 로그인 정보 저장 (ChannelUser)
        // password -> pinHash로 매핑하여 저장
        ChannelUser user = ChannelUser.builder()
                .loginId(request.loginId())
                .pinHash(passwordEncoder.encode(request.password())) // 암호화
                .userUuid(coreResponse.userUuid())   // Core에서 받은 UUID
                .build();

        userRepository.save(user);
    }

    // 로그인
    @Transactional
    public String login(String loginId, String password) {
        // ID 조회
        ChannelUser user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("아이디가 존재하지 않습니다."));

        // 비밀번호 검증 (BCrypt) - pinHash와 비교
        if (!passwordEncoder.matches(password, user.getPinHash())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 마지막 로그인 시간 업데이트
        user.updateLastLogin();

        // 토큰 발급 (UUID를 담아서)
        return jwtUtil.createToken(user.getUserUuid(), user.getLoginId());
    }
}