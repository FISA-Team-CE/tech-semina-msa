package com.techsemina.msa.pointservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointResultEvent {
    private String userId;
    private String status;
}