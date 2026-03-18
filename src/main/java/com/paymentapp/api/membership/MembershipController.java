package com.paymentapp.api.membership;

import com.paymentapp.api.membership.dto.MembershipPolicyResponse;
import com.paymentapp.api.membership.dto.MyMembershipResponse;
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
public class MembershipController {

    private final MembershipService membershipService;

    @GetMapping("/memberships/me")
    public ResponseEntity<MyMembershipResponse> getMyMembership(
            @LoginUser LoginUserInfoDto loginUser) {
        return ResponseEntity.ok(membershipService.getMyMembership(loginUser.id()));
    }

    @GetMapping("/memberships")
    public ResponseEntity<List<MembershipPolicyResponse>> getAllPolicies() {
        return ResponseEntity.ok(membershipService.getAllPolicies());
    }
}
