package com.fisa.channel_service.dto.payment;

import java.math.BigDecimal;

public record AmountRequestDto(String userUuid, BigDecimal amount) {
}
