package com.fisa.core_user_service.dto;

public record UserResponse(
        String userUuid,
        String maskedName,
        String maskedResidentNo
) {
}
