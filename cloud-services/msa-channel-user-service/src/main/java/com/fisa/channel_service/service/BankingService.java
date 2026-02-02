package com.fisa.channel_service.service;

import com.fisa.channel_service.client.CorePaymentClient;
import com.fisa.channel_service.dto.payment.AmountRequestDto;
import com.fisa.channel_service.dto.payment.DepositMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BankingService {

    private final CorePaymentClient corePaymentClient;
    private final KafkaProducerService kafkaProducerService;

    // 계좌 개설 (로그인한 사용자의 UUID로 생성)
    public void createAccount(String userUuid, String accountNo) {
        corePaymentClient.createAccount(accountNo, userUuid);
    }

    // 입금
    public void deposit(String userUuid, String accountNo, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                       throw new IllegalArgumentException("입금 금액은 0보다 커야 합니다.");
        }

        DepositMessage message = new DepositMessage(userUuid, accountNo, amount);
        try {
            kafkaProducerService.sendDepositRequest(message);
        } catch (Exception e) {
            throw new RuntimeException("입금 요청 전송 실패", e);
        }
    }

    // 출금
    public BigDecimal withdraw(String userUuid, String accountNo, BigDecimal amount) {
        // DTO에 userUuid를 함께 담아서 보냄
        return corePaymentClient.withdraw(accountNo, new AmountRequestDto(userUuid, amount));
    }
}