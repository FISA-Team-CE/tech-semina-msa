package com.fisa.channel_service.controller;

import com.fisa.channel_service.dto.user.LoginRequestDto;
import com.fisa.channel_service.dto.user.LoginResponseDto;
import com.fisa.channel_service.dto.user.SignUpRequestDto;
import com.fisa.channel_service.service.ChannelAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/channel/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ChannelAuthService authService;

    @PostMapping("/signup")
    public String signUp(@RequestBody SignUpRequestDto request) {
        authService.signUp(request);
        return "회원가입 성공 (Core 실명인증 + Channel 계정생성 완료)";
    }

    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto request) {
        String token = authService.login(request.loginId(), request.password());
        return new LoginResponseDto(token);
    }
}