package com.example.swp.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VoucherDTO {
    private Integer id;

    @NotBlank(message = "Tên voucher không được để trống")
    @Size(max = 50, message = "Tên voucher không vượt quá 50 ký tự")
    private String name;

    @Size(max = 500, message = "Mô tả không vượt quá 500 ký tự")
    private String description;

    @NotNull(message = "Số tiền giảm giá không được để trống")
    @DecimalMin(value = "1000", message = "Số tiền giảm giá phải lớn hơn 1000")
    private BigDecimal discountAmount;

    @NotNull(message = "Điểm đổi không được để trống")
    @Min(value = 1, message = "Điểm đổi phải lớn hơn 0")
    private Integer requiredPoint;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime endDate;

    @NotNull(message = "Số lượng phát hành không được để trống")
    @Min(value = 1, message = "Số lượng phát hành phải lớn hơn 0")
    private Integer totalQuantity;

    @NotNull(message = "Số lượng còn lại không được để trống")
    @Min(value = 0, message = "Số lượng còn lại không âm")
    private Integer remainQuantity;
    @AssertTrue(message = "Số lượng còn lại không thể lớn hơn số lượng phát hành")
    public boolean isRemainQuantityValid() {
        if (remainQuantity == null || totalQuantity == null) return true;
        return remainQuantity <= totalQuantity;
    }
    @NotNull(message = "Phải chọn trạng thái")
    private String status;
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getRequiredPoint() {
        return requiredPoint;
    }

    public void setRequiredPoint(Integer requiredPoint) {
        this.requiredPoint = requiredPoint;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Integer getRemainQuantity() {
        return remainQuantity;
    }

    public void setRemainQuantity(Integer remainQuantity) {
        this.remainQuantity = remainQuantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

