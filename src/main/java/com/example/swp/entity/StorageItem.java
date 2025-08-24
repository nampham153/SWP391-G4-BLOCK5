package com.example.swp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "storage_item")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StorageItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "storage_id")
    private Integer storageId;

    @Column(name = "zone_id")
    private Integer zoneId;

    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "volume_per_unit")
    private Double volumePerUnit;

    private Integer quantity;

    @Column(name = "date_stored")
    private LocalDate dateStored;
}