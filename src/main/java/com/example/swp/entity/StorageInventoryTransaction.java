package com.example.swp.entity;

import com.example.swp.enums.InventoryTransactionStatus;
import com.example.swp.enums.InventoryTransactionType;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "storage_inventory_transaction")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StorageInventoryTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "transaction_date")
    private OffsetDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private InventoryTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private InventoryTransactionStatus status;

    @Column(name = "customer_id")
    private Integer customerId;

    @Column(name = "storage_id")
    private Integer storageId;

    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "zone_id")
    private Integer zoneId;

    private Integer quantity;

    private String note;

    @Column(name = "rejection_reason")
    private String rejectionReason;
}
