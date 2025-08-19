package com.example.swp.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderRequest {
    private int orderId;

    @Column(nullable = true)
    private LocalDate startDate;

    @Column(nullable = true)
    private LocalDate endDate;

    @Column(nullable = true)
    private LocalDate orderDate;

    private double totalAmount;

    @Column(nullable = true)
    private String status; // PENDING, APPROVED, REJECTED, PAID

    private int customerId;
    private int storageId;
}
