package com.techsemina.msa.pointservice.service;

import com.techsemina.msa.pointservice.domain.Payment;
import com.techsemina.msa.pointservice.dto.CashRequestDTO;
import com.techsemina.msa.pointservice.dto.PaymentRequest;
import com.techsemina.msa.pointservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    // 1. Service + kafka 사용
    private final PointService pointService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentRepository paymentRepository;

    @Transactional // 포인트 차감 중 에러나면 자동 롤백 보장
    public void processCompositePayment(PaymentRequest request) {
        log.info("=== 1. 복합 결제 시작 (Hybrid): User={} ===", request.getLoginId());

        // [Step 0] 장부에 먼저 "결제 대기중(PENDING)"으로 적어놓기
        Payment newPayment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(request.getLoginId())
                .pointAmount(request.getPointAmount())
                .cashAmount(request.getCashAmount())
                .status("PENDING") // 대기중
                .build();

        paymentRepository.save(newPayment); // DB 저장 (INSERT)
        log.info("-> 결제 내역 저장 완료 (PENDING) ✅");

        // [Step 1] 포인트 차감
        pointService.usePoint(request.getLoginId(), request.getPointAmount());
        log.info("-> 포인트 차감 완료 ✅");


        // [Step 2] 현금 출금 요청 (Kafka)
        // 1. 변수에 먼저 담습니다.
        CashRequestDTO cashMessage = new CashRequestDTO(
                request.getOrderId(),
                request.getLoginId(),
                request.getCashAmount()
        );
        // 2. 보내기 전에 로그 확인
        log.info("-> [Kafka 전송] 토픽: core-withdraw-request, 데이터: {}", cashMessage);
        // 3. 전송
        kafkaTemplate.send("core-withdraw-request", cashMessage);

        log.info("=== 2. 결제 요청 접수 완료 (결과는 비동기 처리) ⏳ ===");
    }


    // ✅ 결제 성공 확정 (Commit)
    @Transactional
    public void completePayment(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("주문 없음"));

        payment.setStatus("COMPLETED");
        log.info("🎉 최종 결제 완료 처리됨: {}", orderId);
    }

    // ✅ [추가 2] 결제 실패 보상 (Rollback/Refund)
    @Transactional
    public void compensatePayment(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("주문 없음"));

        // 이미 취소된 건지 체크하는 로직 등이 여기 들어가면 안전함
        if ("FAILED".equals(payment.getStatus())) return;

        // 포인트 환불 로직
        pointService.refundPoint(payment.getUserId(), payment.getPointAmount());

        payment.setStatus("FAILED");
        log.info("🚨 보상 트랜잭션(환불) 완료: {}", orderId);
    }

}