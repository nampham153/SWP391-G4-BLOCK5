package com.example.swp.dto;

import com.example.swp.enums.InventoryTransactionType;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryRequestCreateDTO {
    private Integer orderId;
    private InventoryTransactionType transactionType;
    private String itemName;
    private Double volumePerUnit;
    private Integer quantity;
    private String note;
}

