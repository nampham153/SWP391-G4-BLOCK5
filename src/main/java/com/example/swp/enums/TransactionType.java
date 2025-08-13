package com.example.swp.enums;

public enum TransactionType {
    PAID,
    REQUESTED, // Khách gửi yêu cầu
    APPROVED,  // Staff duyệt hoàn tiền
    DENIED,
    RENT,
    PENDING,// Staff từ chối hoàn tiền
    REFUND     // Đã hoàn tiền xong
}
