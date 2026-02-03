package com.techsemina.msa.pointservice.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class OrderIdGenerator {

    // 결과 예시: PAY-20260203-163055-19283
    public static String generateOrderId() {
        // 1. 날짜/시간 (초 단위까지)
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        // 2. 랜덤 숫자 (5자리) -> 실제로는 Redis 등에서 번호를 따오는 게 정석이지만, 지금은 랜덤으로
        int randomNo = ThreadLocalRandom.current().nextInt(10000, 99999);

        // 3. 조합
        return "PAY-" + timestamp + "-" + randomNo;
    }
}