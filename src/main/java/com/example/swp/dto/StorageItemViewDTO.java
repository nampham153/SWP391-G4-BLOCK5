package com.example.swp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class StorageItemViewDTO {
    private Integer id;
    private String storageName;
    private String zoneName;
    private String itemName;
    private Double volumePerUnit;
    private Integer quantity;
    private LocalDate dateStored;
}
