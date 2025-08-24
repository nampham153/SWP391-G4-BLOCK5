package com.example.swp.service.impl;

import com.example.swp.config.VNPayConfig;
import com.example.swp.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.bind.DatatypeConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@RequiredArgsConstructor
public class VnPayServiceimpl implements VNPayService {
    private final VNPayConfig vnPayConfig;
    @Override
    public String createVNPayUrl(HttpServletRequest req, long amount, int orderId) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType   = "other";

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version",  vnp_Version);
        vnp_Params.put("vnp_Command",  vnp_Command);
        vnp_Params.put("vnp_TmnCode",  vnPayConfig.getTmnCode());
        vnp_Params.put("vnp_Amount",   String.valueOf(amount * 100)); // cents
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef",   String.valueOf(orderId));
        vnp_Params.put("vnp_OrderInfo","Thanh toan don hang:" + orderId);
        vnp_Params.put("vnp_OrderType",orderType);
        vnp_Params.put("vnp_Locale",   "vn");
        vnp_Params.put("vnp_ReturnUrl",vnPayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr",   req.getRemoteAddr());

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        String vnp_CreateDate = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query    = new StringBuilder();

        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String value = vnp_Params.get(fieldName);
            if (value != null && !value.isEmpty()) {
                String enc = java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.US_ASCII);
                hashData.append(fieldName).append('=').append(enc);
                query.append(fieldName).append('=').append(enc);
                if (i < fieldNames.size() - 1) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String secureHash = VNPayService.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);
        return vnPayConfig.getPayUrl() + "?" + query;
    }


//    public String createVnpayUrl(String orderId, double amount, String returnUrl) {
//        String vnp_Version = "2.1.0";
//        String vnp_Command = "pay";
//        String vnp_TmnCode = "V12NTJPS";
//        String vnp_HashSecret = "U6QYGRTSYARF6YEHBNLKAXKNADGIUSVN";
//        String vnp_OrderInfo = "Thanh toan don hang: " + orderId;
//        String vnp_TxnRef = orderId;
//        String vnp_Amount = String.valueOf(amount * 100); // VNPAY dùng đơn vị "đồng x 100"
//        String vnp_Locale = "vn";
//        String vnp_CurrCode = "VND";
//        String vnp_IpAddr = "127.0.0.1";
//        String vnp_ReturnUrl = returnUrl;
//        String vnp_CreateDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
//
//        Map<String, String> vnp_Params = new HashMap<>();
//        vnp_Params.put("vnp_Version", vnp_Version);
//        vnp_Params.put("vnp_Command", vnp_Command);
//        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
//        vnp_Params.put("vnp_Amount", vnp_Amount);
//        vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
//        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
//        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
//        vnp_Params.put("vnp_Locale", vnp_Locale);
//        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
//        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
//        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
//
//        // Sort và build query
//        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
//        Collections.sort(fieldNames);
//        StringBuilder hashData = new StringBuilder();
//        StringBuilder query = new StringBuilder();
//        for (String fieldName : fieldNames) {
//            String value = vnp_Params.get(fieldName);
//            if (hashData.length() > 0) {
//                hashData.append('&');
//                query.append('&');
//            }
//            hashData.append(fieldName).append('=').append(value);
//            query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
//                    .append('=')
//                    .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
//        }
//
//        // Generate secureHash
//        String secureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
//        query.append("&vnp_SecureHash=").append(secureHash);
//
//        return "https://pay.vnpay.vn/vpcpay.html?" + query;
//    }



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
