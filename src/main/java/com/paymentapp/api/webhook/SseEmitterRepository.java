package com.paymentapp.api.webhook;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterRepository {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void save(String key, SseEmitter emitter) {
        emitters.put(key, emitter);
    }

    public SseEmitter get(String key) {
        return emitters.get(key);
    }

    public void delete(String key) {
        emitters.remove(key);
    }
}