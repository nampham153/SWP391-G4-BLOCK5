package com.example.swp.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.bind.DatatypeConverter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public interface VNPayService {
    public String createVNPayUrl(HttpServletRequest req, long amount, int orderId) throws UnsupportedEncodingException;

//    public String createVnpayUrl(String orderId, double amount, String returnUrl);

    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            hmac512.init(secretKeySpec);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return DatatypeConverter.printHexBinary(bytes).toUpperCase();
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi tạo hash", ex);
        }
    }
}
