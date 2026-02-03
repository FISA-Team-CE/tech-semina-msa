package com.fisa.core_payment_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fisa.core_payment_service.domain.Account;
import com.fisa.core_payment_service.dto.CashRequestDTO;
import com.fisa.core_payment_service.dto.CashResponseDTO;
import com.fisa.core_payment_service.dto.CouponIssueMessage;
import com.fisa.core_payment_service.dto.DepositMessage;
import com.fisa.core_payment_service.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;
    private final AccountRepository accountRepository;
    private final KafkaProducerService kafkaProducerService;

    // 1. 입금 처리
    @KafkaListener(topics = "bank_deposit", groupId = "core-group")
    public void consumeDeposit(String message) {
        try {
            DepositMessage depositDto = objectMapper.readValue(message, DepositMessage.class);

            paymentService.deposit(
                    depositDto.getAccountNo(), // ★ 수정됨 (getAccountNumber() -> getAccountNo())
                    depositDto.getUserUuid(),
                    depositDto.getAmount()
            );

            log.info("💰 [Core] 입금 처리 완료: {}", depositDto);

        } catch (JsonProcessingException e) {
            log.error("❌ JSON 파싱 에러: {}", message, e);
        } catch (Exception e) {
            log.error("❌ 입금 처리 중 에러: {}", e.getMessage());
        }
    }


    // 2. 현금 출금 처리 (PointService와 연결)
    @KafkaListener(topics = "core-withdraw-request", groupId = "core-group")
    public void consumeWithdraw(String message) {

        CashRequestDTO requestDto = null;

        try {
            // (1) 메시지 파싱
            requestDto = objectMapper.readValue(message, CashRequestDTO.class);
            log.info("📉 [Core] 출금 요청 수신: {}", requestDto);

            // (2) 계좌 조회
            Account account = accountRepository.findByUserUuid(requestDto.getLoginId())
                    .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다."));

            // (3) 출금 비즈니스 로직
            paymentService.withdraw(
                    account.getAccountNo(),
                    requestDto.getLoginId(),
                    BigDecimal.valueOf(requestDto.getAmount())
            );

            // (4) 성공 이벤트 발행 -> PointService의 토픽 이름인 "core-result"로 변경
            CashResponseDTO successResponse = new CashResponseDTO(
                    requestDto.getOrderId(),
                    requestDto.getLoginId(), // ★ userUuid 추가 (PointService 환불용)
                    "SUCCESS",
                    "정상 출금 완료"
            );

            // ★ 토픽 이름 변경: core-withdraw-result -> core-result
            kafkaProducerService.send("core-result", successResponse);
            log.info("✅ [Core] 출금 성공 -> Point Service로 전송: {}", successResponse);

        } catch (JsonProcessingException e) {
            log.error("❌ JSON 파싱 에러 (Withdraw): {}", message, e);
        } catch (Exception e) {
            log.error("❌ 출금 처리 실패: {}", e.getMessage());

            if (requestDto != null) {
                // (5) 실패 이벤트 발행
                CashResponseDTO failResponse = new CashResponseDTO(
                        requestDto.getOrderId(),
                        requestDto.getLoginId(), // ★ userUuid 추가
                        "FAIL",
                        e.getMessage()
                );
                // ★ 토픽 이름 변경
                kafkaProducerService.send("core-result", failResponse);
                log.info("⚠️ [Core] 출금 실패 -> Point Service로 전송: {}", failResponse);
            }
        }
    }
}