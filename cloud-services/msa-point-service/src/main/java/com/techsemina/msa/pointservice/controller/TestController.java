package com.techsemina.msa.pointservice.controller;

package com.techsemina.msa.pointservice.controller;

import com.techsemina.msa.pointservice.dto.CashResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // 🕵️‍♂️ 가짜 현금 서비스: "성공했다"고 뻥치기
    // 호출 주소: POST /test/fake-success?orderId=PAY-1234
    @PostMapping("/fake-success")
    public String fakeSuccess(@RequestParam String orderId) {

        // 현금 서비스가 보내줄 법한 메시지를 우리가 직접 만듭니다.
        CashResponseDTO fakeResponse = new CashResponseDTO(orderId, "SUCCESS", "정상 처리됨");

        // 'core-withdraw-result' 토픽으로 쏩니다.
        // 그러면 아까 만든 PaymentConsumer가 이걸 낚아채서 'completePayment'를 실행하겠죠?
        kafkaTemplate.send("core-withdraw-result", fakeResponse);

        return "가짜 성공 메시지 전송 완료! (OrderID: " + orderId + ")";
    }

    // 🕵️‍♂️ 가짜 현금 서비스: "실패했다"고 뻥치기 (롤백 테스트)
    @PostMapping("/fake-fail")
    public String fakeFail(@RequestParam String orderId) {
        CashResponseDTO fakeResponse = new CashResponseDTO(orderId, "FAILED", "잔액 부족");
        kafkaTemplate.send("core-withdraw-result", fakeResponse);
        return "가짜 실패 메시지 전송 완료 -> 환불될 것임";
    }
}