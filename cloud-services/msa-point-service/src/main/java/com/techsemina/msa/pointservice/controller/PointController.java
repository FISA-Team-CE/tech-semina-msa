package com.techsemina.msa.pointservice.controller;

import com.techsemina.msa.pointservice.domain.PointMaster;
import com.techsemina.msa.pointservice.dto.PointRequestDTO;
import com.techsemina.msa.pointservice.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/point") // 👈 포인트 관련은 여기서 처리
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    /**
     * Charge a user's account with the specified point amount.
     *
     * @param dto request body containing the user's loginId and the pointAmount to add
     * @return the updated PointMaster wrapped in a ResponseEntity
     */
    @PostMapping("/charge")
    public ResponseEntity<PointMaster> charge(@RequestBody PointRequestDTO dto) {

        PointMaster result = pointService.chargePoint(
                dto.getLoginId(),
                dto.getPointAmount()
        );

        return ResponseEntity.ok(result);
    }
}