package com.techsemina.msa.pointservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoreResultEvent {
    private String orderId;
    private String userId;
    private String status; // "SUCCESS" 또는 "FAIL"
}