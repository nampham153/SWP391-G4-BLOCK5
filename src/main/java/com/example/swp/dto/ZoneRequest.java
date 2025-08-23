package com.example.swp.dto;

import com.example.swp.enums.ZoneStatus;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ZoneRequest {
    @NotBlank(message = "Tên khu vực không được để trống")
    private String name;
    
    @NotNull(message = "Diện tích không được để trống")
    @DecimalMin(value = "0.1", message = "Diện tích phải lớn hơn 0.1 m²")
    private Double zoneArea;
    
    @NotNull(message = "Giá thuê không được để trống")
    @DecimalMin(value = "5000000", message = "Giá thuê phải từ 5,000,000 VNĐ trở lên")
    @DecimalMax(value = "25000000", message = "Giá thuê không được vượt quá 25,000,000 VNĐ")
    private Double pricePerDay;
    
    private ZoneStatus status;
    private Integer storageId;
}
