package com.paymentapp.core.security.oauth2;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    @Getter
    private final Long id;
    @Getter
    private final String email;
    private final OAuth2User oAuth2User;

    public CustomOAuth2User(Long id, String email, OAuth2User oAuth2User) {
        this.id = id;
        this.email = email;
        this.oAuth2User = oAuth2User;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oAuth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return oAuth2User.getName();
    }
}