package com.example.swp.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;

@Component
public class OtpCache {
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void put(String email, String otp) {
        cache.put(email, otp);
        // tự động xóa sau 5 phút
        scheduler.schedule(() -> cache.remove(email), 5, TimeUnit.MINUTES);
    }

    public String get(String email) {
        return cache.get(email);
    }

    public void remove(String email) {
        cache.remove(email);
    }
}

