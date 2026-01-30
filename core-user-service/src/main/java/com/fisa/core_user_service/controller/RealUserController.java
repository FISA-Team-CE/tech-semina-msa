package com.fisa.core_user_service.controller;

import com.fisa.core_user_service.domain.RealUser;
import com.fisa.core_user_service.dto.RegisterRequest;
import com.fisa.core_user_service.dto.UserResponse;
import com.fisa.core_user_service.service.RealUserService;
import com.fisa.core_user_service.util.MaskingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/core/users")
@RequiredArgsConstructor
public class RealUserController {

    private final RealUserService realUserService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        RealUser user = realUserService.registerUser(
                request.username(),
                request.realName(),
                request.residentNo()
        );

        // 내부 DB에는 암호화된 실명
        // 밖으로 나갈 때는 마스킹 처리
        return ResponseEntity.ok(new UserResponse(
                user.getUserUuid().toString(),
                MaskingUtil.maskName(user.getRealName()), // 홍길동 -> 홍*동
                MaskingUtil.maskResidentNo(user.getResidentNo()) // 900101-1******
        ));
    }
}