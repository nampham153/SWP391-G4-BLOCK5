package com.example.swp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recent_activity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String action; // Nội dung hành động, ví dụ: "Login", "Create voucher"

    private String entityName;     // Ví dụ: "Voucher #123", "Order #999"
    private String entityType;     // Ví dụ: "Voucher", "Order", "User"
    private String detailLink;     // Đường dẫn đến chi tiết nếu có

    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Thông tin người thực hiện: có thể là Customer hoặc Staff

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Manager manager;

    // Để lọc nhanh người thực hiện dạng text
    @Column(nullable = false)
    private String actor;
}
