package com.fisa.core_payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CouponIssueMessage {

    private String userUuid;
    private String couponCode;
    private String description;
}
