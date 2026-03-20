package com.paymentapp.front.properties;

import com.paymentapp.api.webhook.Webhook;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "portone")
public class PortOneProperties {
    private Api api;
    private Store store;
    private Map<String, String> channel;
    private Webhook webhook;

    @Data
    public static class Api {
        private String baseUrl;
        private String secret;
    }

    @Data
    public static class Store {
        private String id;
    }

    @Data
    public static class Webhook {
        private String secret;
    }
}
