package com.example.swp.dto;

import com.example.swp.entity.Customer;
import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.util.Date;

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
