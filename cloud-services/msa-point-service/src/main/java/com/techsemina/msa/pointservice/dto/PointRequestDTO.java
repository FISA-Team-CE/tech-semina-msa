package com.techsemina.msa.pointservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PointRequestDTO {
    private String loginId;
    private Long pointAmount; // 포인트 차감/환불 액수
}