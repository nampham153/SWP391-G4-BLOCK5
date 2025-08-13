package com.example.swp.entity;

import com.example.swp.enums.VoucherStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vouchers")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String description;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "required_point")
    private Integer requiredPoint;

    @Enumerated(EnumType.STRING)
    private VoucherStatus status; // Enum: ACTIVE, INACTIVE, EXPIRED

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "remain_quantity")
    private Integer remainQuantity;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    // Getter, Setter, Constructors
}

