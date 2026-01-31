package fisa.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CouponIssueEvent {
    private String userUuid;        // 사용자 UUID
    private String couponCode;      // 쿠폰 코드
    private String description;     // 쿠폰 설명
}