package com.fisa.channel_service.client;

import com.fisa.channel_service.dto.user.CoreUserRequestDto;
import com.fisa.channel_service.dto.user.CoreUserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// name: 서비스 이름, url: 로컬 테스트용 주소 (나중에 Tailscale IP로 변경됨)
// TODO : port 확인 필요
@FeignClient(name = "core-user-service", url = "http://localhost:8082")
public interface CoreUserClient {

    @PostMapping("/api/core/users/register")
    CoreUserResponseDto registerUser(@RequestBody CoreUserRequestDto request);
}