package com.fisa.channel_service.dto.user;

public record SignUpRequestDto(
        String loginId, String password, String realName, String residentNo
) {
}
