package com.example.swp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;


import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders") // Đặt tên khác vì "Order" là từ khóa SQL
public class Order {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        @Column(nullable = true)
        private LocalDate startDate;

        @Column(nullable = true)
        private LocalDate endDate;

        @Column(nullable = true)
        private LocalDate orderDate;

        private double totalAmount;

        @Column(nullable = true)
        private String status; // PENDING, APPROVED, REJECTED, PAID

        @ManyToOne
        @JoinColumn(name = "customer_id", nullable = true)
        @JsonIgnore
        private Customer customer;

        @ManyToOne
        @JoinColumn(name = "voucher_id") // tên cột foreign key trong bảng orders
        private Voucher voucher;

        @ManyToOne
        @JoinColumn(name = "staff_id")
        @JsonIgnore
        private Staff staff;

        @ManyToOne
        @JoinColumn(name = "manager_id")
        @JsonIgnore
        private Manager manager;

        @ManyToOne
        @JoinColumn(name = "zone_id", nullable = true)
        @JsonIgnore
        private Zone zone;
        @Column(length = 500)
        private String cancelReason;


        @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
        private EContract eContract;

        public Order(int id) {
                this.id = id;
        }
}




