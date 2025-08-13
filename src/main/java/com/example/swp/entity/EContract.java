package com.example.swp.entity;

import com.example.swp.enums.EContractStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "econtract")
public class EContract {



    // Trong Order.java
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_code", unique = true)
    private String contractCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EContractStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Column(name = "price_per_day", nullable = false)
    private Double pricePerDay;

    @Column(name = "rental_area", nullable = false)
    private Double rentalArea;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "storage_name", nullable = false)
    private String storageName;

    @OneToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private Order order;

    // ===== AUTO SET CREATED_AT AND STATUS =====
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = EContractStatus.PENDING;
        }
    }

    // ===== GETTERS & SETTERS =====

    public Long getId() {
        return id;
    }

    public String getContractCode() {
        return contractCode;
    }

    public void setContractCode(String contractCode) {
        this.contractCode = contractCode;
    }

    public EContractStatus getStatus() {
        return status;
    }

    public void setStatus(EContractStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(LocalDateTime signedAt) {
        this.signedAt = signedAt;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Double getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(Double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public Double getRentalArea() {
        return rentalArea;
    }

    public void setRentalArea(Double rentalArea) {
        this.rentalArea = rentalArea;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStorageName() {
        return storageName;
    }

    public void setStorageName(String storageName) {
        this.storageName = storageName;
    }
}
