package com.fisa.core_user_service.dto;

public record RegisterRequest(

        String username,
        String realName,
        String residentNo
) {
}
