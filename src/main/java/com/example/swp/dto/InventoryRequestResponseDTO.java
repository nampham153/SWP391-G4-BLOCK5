package com.example.swp.dto;

import com.example.swp.enums.InventoryTransactionStatus;
import com.example.swp.enums.InventoryTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data @NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryRequestResponseDTO {
    private Integer id;
    private OffsetDateTime transactionDate;
    private InventoryTransactionType transactionType;
    private InventoryTransactionStatus status;
    private Integer orderId;
    private Integer storageId;
    private String storageName;
    private Integer zoneId;
    private String zoneName;
    private Integer quantity;
    private String itemName;
    private Double volumePerUnit;
    private String note;
    private String rejectionReason;
}

