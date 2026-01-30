package com.fisa.core_payment_service.dto;

import java.math.BigDecimal;

public record AmountRequestDto(String userUuid, BigDecimal amount) {
}
