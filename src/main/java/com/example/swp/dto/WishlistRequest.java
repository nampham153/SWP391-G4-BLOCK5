package com.example.swp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistRequest {
    private Long storageId; // hoặc int storageId, tùy entity của bạn
    // Mở rộng thêm trường nếu muốn (ghi chú, priority, ...)
}
