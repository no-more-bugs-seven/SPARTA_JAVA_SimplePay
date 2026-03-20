package com.paymentapp.api.membership;

import com.paymentapp.api.member.Member;
import com.paymentapp.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "membership_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MembershipHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tier_id", nullable = false)
    private MembershipTier tier;


    @Builder
    private MembershipHistory(Member member, MembershipTier tier) {
        this.member = member;
        this.tier = tier;
    }
}