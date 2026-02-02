package com.techsemina.msa.pointservice.controller;

import com.techsemina.msa.pointservice.dto.PaymentRequest;
import com.techsemina.msa.pointservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // 스프링이 미리 만들어둔 객체를 주입받음 (DI)
    private final PaymentService paymentService;


    @PostMapping("/payment")
    public String pay(@RequestBody PaymentRequest dto) {
        paymentService.processCompositePayment(dto);
        return "결제 요청 처리 완료";
    }
}