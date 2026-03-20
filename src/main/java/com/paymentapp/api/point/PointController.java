package com.paymentapp.api.point;

import com.paymentapp.api.point.dto.PointTransactionResponse;
import com.paymentapp.core.annotation.LoginUser;
import com.paymentapp.core.dto.LoginUserInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PointController {

    private final PointService pointService;

    @GetMapping("/points/transactions/me")
    public ResponseEntity<List<PointTransactionResponse>> getMyTransactions(
            @LoginUser LoginUserInfoDto loginUser) {
        return ResponseEntity.ok(pointService.getMyTransactions(loginUser.id()));
    }
}