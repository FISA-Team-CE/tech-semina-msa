package com.techsemina.msa.pointservice.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class OrderIdGenerator {

    // 결과 예시: PAY-20260203-163055-19283
    public static String generateOrderId() {

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS"));
        String randomNo = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return "PAY-" + timestamp + "-" + randomNo;
    }
}