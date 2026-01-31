package com.fisa.core_user_service.util;

public class MaskingUtil {

    // 이름 마스킹: 홍길동 -> 홍*동, 남궁민수 -> 남궁*수
    public static String maskName(String name) {
        if (name == null || name.length() < 2) return name;

        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }

        // 가운데 글자들을 *로 변경
        String first = name.substring(0, 1);
        String last = name.substring(name.length() - 1);
        String middle = "*".repeat(name.length() - 2);

        return first + middle + last;
    }

    // 주민번호 마스킹 (뒷자리)
    public static String maskResidentNo(String residentNo) {

        if (residentNo == null || residentNo.length() < 14) return residentNo;

        return residentNo.substring(0, 8) + "******";
    }
}