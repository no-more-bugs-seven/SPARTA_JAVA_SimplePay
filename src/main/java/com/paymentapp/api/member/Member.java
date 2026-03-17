package com.paymentapp.api.member;

import com.paymentapp.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// @AllArgsConstructor 지양: 필드 순서 변경 시 위험
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100) // OAuth2 로그인 시 비밀번호가 없을 수 있으므로 nullable = true (기본값)
    private String password;

    /*
    Instagram 특성상 이메일 가입 또는 전화번호 가입이 가능하므로 Nullable
    대신 "둘 중 하나는 반드시 존재해야 한다"는 검증 로직은
    DB 레벨이 아닌, 잠시 후 구현할 Service 레벨
    (signUp 메서드)에서 수행
    */
    @Column(unique = true, length = 100)
    private String email;

    @Column(unique = true, length = 20) // Nullable (이메일로 가입한 경우)
    private String phone;

    @Column(nullable = false, length = 50)
    private String name;

    @Column()
    private Long pointBalance;

    @Column()
    private Long membershipTierId;

    @Builder
    private Member(String password, String email, String phone, String name, Long pointBalance) {
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.name = name;
        this.pointBalance = pointBalance;
    }
}