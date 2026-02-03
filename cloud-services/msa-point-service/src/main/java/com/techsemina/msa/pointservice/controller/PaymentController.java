package com.techsemina.msa.pointservice.controller;

import com.techsemina.msa.pointservice.dto.PaymentRequest;
import com.techsemina.msa.pointservice.dto.PaymentResponse;
import com.techsemina.msa.pointservice.service.PaymentService;
import com.techsemina.msa.pointservice.util.OrderIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    // 스프링이 미리 만들어둔 객체를 주입받음 (DI)
    private final PaymentService paymentService;


    @PostMapping("/payment")
    public ResponseEntity<PaymentResponse> pay(@RequestBody PaymentRequest dto) {
        // 1. 주문 번호 생성 및 주입
        // 프론트에서 안 보냈으면(null이면) 유틸리티로 생성
        if (dto.getOrderId() == null) {
            String newId = OrderIdGenerator.generateOrderId(); // "PAY-2026..." 생성
            dto.setOrderId(newId); // DTO에 쏙 넣기
        }

        // 2. ID가 채워진 dto를 서비스로 넘김 (Kafka로 메시지 던지고 바로 리턴)
        paymentService.processCompositePayment(dto);

        // 2. 응답 객체 생성
        PaymentResponse response = PaymentResponse.builder()
                .message("결제 요청이 정상적으로 접수되었습니다. (결과 알림 예정)")
                .orderId(dto.getOrderId())
                .status("PENDING")         // 아직 Kafka 타고 가는 중 - PENDING
                .build();

        // 3. 202 Accepted 리턴
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}