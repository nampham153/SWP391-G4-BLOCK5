package com.example.swp.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
        @JoinColumn(name = "storage_id", nullable = true)
        private Storage storage;

        // Diện tích thuê (m2) cho đơn hàng này
        @Column(nullable = true)
        private double rentalArea;

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
        
        // Lưu danh sách chỉ số ô 50m² đã chọn (CSV: "0,1,5,6")
        @Column(name = "selected_unit_indices", length = 1000)
        private String selectedUnitIndices;
        
        // Lưu danh sách zone IDs đã chọn (CSV: "1,2,3")
        @Column(name = "selected_zone_ids", length = 500)
        private String selectedZoneIds;
        
        @Column(length = 500)
        private String cancelReason;


        @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
        @JsonIgnore
        private EContract eContract;

        public Order(int id) {
                this.id = id;
        }
}




