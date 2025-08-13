package com.example.swp.entity;

import com.example.swp.enums.ActivityType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action; // Mô tả ngắn: "Tạo đơn hàng", "Thanh toán", ...

    @Column(length = 1000)
    private String description; // Nội dung chi tiết
    @CreationTimestamp
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // Quan hệ đến các đối tượng liên quan (nếu có)
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = true)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "transaction_id", nullable = true)
    private StorageTransaction transaction;

    @ManyToOne
    @JoinColumn(name = "payment_id", nullable = true)
    private Payment payment;

    @ManyToOne
    @JoinColumn(name = "feedback_id", nullable = true)
    private Feedback feedback;

    @ManyToOne
    @JoinColumn(name = "issue_id", nullable = true)
    private Issue issue;

    // Constructors
    public ActivityLog() {
        this.timestamp = LocalDateTime.now();
    }

    public ActivityLog(String action, String description, Customer customer) {
        this.action = action;
        this.description = description;
        this.customer = customer;
        this.timestamp = LocalDateTime.now();
    }
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ActivityType type;



}
