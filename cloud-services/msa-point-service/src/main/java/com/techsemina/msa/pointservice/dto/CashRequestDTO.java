package com.techsemina.msa.pointservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CashRequestDTO {
    private String loginId;
    private Long amount; // 출금 액수
}