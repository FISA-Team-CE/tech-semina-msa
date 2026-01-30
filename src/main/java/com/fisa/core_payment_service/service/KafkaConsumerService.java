package com.fisa.core_payment_service.service;

import com.fasterxml.jackson.core.JsonProcessingException; // 예외 처리용 추가
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fisa.core_payment_service.dto.CouponIssueMessage;
import com.fisa.core_payment_service.dto.DepositMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final PaymentService paymentService;
    private final CouponManageService couponService;
    private final ObjectMapper objectMapper;

    //  입금 처리
    @KafkaListener(topics = "bank_deposit", groupId = "core-group")
    public void consumeDeposit(String message) {
        try {

            DepositMessage depositDto = objectMapper.readValue(message, DepositMessage.class);

            paymentService.deposit(
                    depositDto.getUserUuid(),      // 사용자 ID (String)
                    depositDto.getAccountNumber(), // 계좌번호 (String)
                    depositDto.getAmount()         // 금액 (BigDecimal)
            );

            log.info("💰 [Core] 입금 처리 완료: {}", depositDto);

        } catch (JsonProcessingException e) {
            log.error("❌ JSON 파싱 에러: {}", message, e);
        }
    }

    // 쿠폰 발급 처리
    @KafkaListener(topics = "coupon_issue", groupId = "core-group")
    public void consumeCouponIssue(String message) {
        try {
            CouponIssueMessage couponDto = objectMapper.readValue(message, CouponIssueMessage.class);
            couponService.issueCoupon(couponDto);
        } catch (JsonProcessingException e) {
            log.error("❌ JSON 파싱 에러: {}", message, e);
        }
    }
}