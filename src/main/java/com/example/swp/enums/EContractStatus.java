package com.example.swp.enums;

public enum EContractStatus {
    PENDING,                // Mới tạo, chờ ký
    SIGNED,                 // Khách hàng đã ký
    APPROVED,               // Quản lý đã duyệt
    REJECTED,               // Quản lý từ chối
    CANCELLED,              // Hợp đồng đã bị hủy
    PENDING_CANCELLATION    // Chờ admin xác nhận hủy
}
