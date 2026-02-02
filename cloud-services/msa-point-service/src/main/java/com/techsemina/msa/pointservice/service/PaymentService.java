package com.techsemina.msa.pointservice.service;

import com.techsemina.msa.pointservice.dto.CashRequestDTO;
import com.techsemina.msa.pointservice.dto.PaymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    // 1. 이제 API Client 대신, 옆자리 동료(Service)와 우체부(Kafka)를 사용
    private final PointService pointService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Processes a hybrid payment by deducting points locally and sending an asynchronous cash withdrawal request.
     *
     * Performs a local point deduction within the current transaction and then publishes a cash withdrawal request
     * to the "core-withdraw-request" Kafka topic; the withdrawal outcome is handled asynchronously and is not rolled back
     * as part of the local transaction.
     *
     * @param request payment details containing the user's login ID, point amount to deduct, and cash amount to withdraw
     */
    @Transactional // 포인트 차감 중 에러나면 자동 롤백 보장
    public void processCompositePayment(PaymentRequest request) {
        log.info("=== 1. 복합 결제 시작 (Hybrid): User={} ===", request.getLoginId());

        // [Step 1] 포인트 차감 (Local Logic)
        // -> 같은 프로젝트라 네트워크를 안 타므로 try-catch가 굳이 필요 없음
        // -> 실패하면 RuntimeException이 터지면서 트랜잭션이 전체 롤백됨
        log.info("-> [Local] 포인트 서비스 직접 호출: {}점 차감", request.getPointAmount());
        pointService.usePoint(request.getLoginId(), request.getPointAmount());
        log.info("-> 포인트 차감 완료 (DB 반영됨) ✅");

        // [Step 2] 현금 출금 요청 (Async Kafka)
        // -> 핵심 변경점: 결과를 기다리지(Block) 않고 쪽지만 보냄
        // -> 따라서 여기서 '실패 시 롤백' 코드를 짤 필요가 없음 (Consumer가 할 일)
        log.info("-> [Remote] 코어뱅킹 출금 요청 전송 (Kafka)");
        kafkaTemplate.send("core-withdraw-request",
                new CashRequestDTO(request.getLoginId(), request.getCashAmount()));

        // 사용자는 여기서 즉시 응답을 받습니다. (대기 시간 0초)
        log.info("=== 2. 결제 요청 접수 완료 (결과는 비동기 처리) ⏳ ===");
    }
}