package com.techsemina.msa.pointservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private String errorCode;    // 예: "SERVER_ERROR", "INVALID_INPUT"
    private String errorMessage; // 예: "DB 연결에 실패했습니다."
}