package com.techsemina.msa.pointservice.kafka;

import com.techsemina.msa.pointservice.dto.CashRequestDTO;
import com.techsemina.msa.pointservice.dto.CoreResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Slf4j
@RequiredArgsConstructor
public class MockCoreBanking {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Random random = new Random();

    /**
     * Consumes a core withdraw request from Kafka, simulates bank processing with a short delay, determines success or failure randomly, and publishes a CoreResultEvent containing the request's loginId and resulting status.
     *
     * @param request the withdrawal request containing the user's loginId and the requested amount (in KRW)
     * @throws InterruptedException if the simulated processing delay is interrupted
     */
    @KafkaListener(topics = "core-withdraw-request", groupId = "mock-core-group")
    public void handleWithdrawRequest(CashRequestDTO request) throws InterruptedException {
        log.info("============== [On-Premise 시뮬레이터] ==============");
        log.info("🤑 코어뱅킹: 출금 요청 받음! 금액={}원", request.getAmount());

        // 1. 실제 은행처럼 약간의 딜레이(2초)를 줍니다.
        Thread.sleep(2000);

        // 2. 랜덤하게 성공/실패 결정 (50% 확률)
        boolean isSuccess = random.nextBoolean();
        // 테스트하고 싶은 시나리오에 따라 강제로 true/false로 바꿔보세요!

        String status = isSuccess ? "SUCCESS" : "FAIL";
        log.info("🏦 코어뱅킹 처리 결과: {}", status);

        // 3. 결과 메시지 발송 (-> PaymentKafkaConsumer가 받음)
        kafkaTemplate.send("core-result", new CoreResultEvent(request.getLoginId(), status));
        log.info("===================================================");
    }
}