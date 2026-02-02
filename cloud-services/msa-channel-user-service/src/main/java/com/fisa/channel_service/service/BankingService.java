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
    public BigDecimal deposit(String userUuid, String accountNo, BigDecimal amount) {
        DepositMessage message = new DepositMessage(userUuid, accountNo, amount);
        kafkaProducerService.sendDepositRequest(message);

        // 비동기라 결과가 바로 안 옴 -> 일단 0원 리턴
        return BigDecimal.ZERO;
    }

    // 출금
    public BigDecimal withdraw(String userUuid, String accountNo, BigDecimal amount) {
        // DTO에 userUuid를 함께 담아서 보냄
        return corePaymentClient.withdraw(accountNo, new AmountRequestDto(userUuid, amount));
    }
}