package com.paymentapp.front.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pages")
public class PageController {

    @GetMapping("/shop")
    public String shop() {
        return "shop";
    }

    @GetMapping("/orders")
    public String orders() {
        return "orders";
    }

    @GetMapping("/points")
    public String points() {
        return "points";
    }

    @GetMapping("/plans")
    public String plans() {
        return "plans";
    }

    @GetMapping("/subscribe")
    public String subscribe() {
        return "subscribe";
    }

    @GetMapping("/subscriptions")
    public String subscriptions() {
        return "subscriptions";
    }

    // 구독 플랜 변경 페이지
    @GetMapping("/subscription-change")
    public String subscriptionChange() {
        return "subscription-change";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }
}