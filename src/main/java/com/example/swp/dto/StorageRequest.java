package com.example.swp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Component
public class StorageRequest {
    @NotBlank(message = "Tên kho không được để trống")
    private String storagename;
    @NotBlank(message = "Địa chỉ kho không được để trống")
    private String address;
    @NotBlank(message = "Thành phố không được để trống")
    private String city;
    @NotBlank(message = "Quận/huyện không được để trống")
    private String state;
    @NotNull(message = "Diện tích không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Diện tích không được âm")
    private double area;
    @NotNull(message = "Giá thuê không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá thuê không được âm")
    private double pricePerDay;
    private String description;
    private String imUrl;
    private boolean status;
}
