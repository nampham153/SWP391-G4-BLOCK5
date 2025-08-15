package com.example.swp.repository;

import com.example.swp.entity.ActivityLog;
import com.example.swp.entity.Customer;
import com.example.swp.enums.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    // Lấy toàn bộ hoạt động của khách hàng, sắp xếp mới nhất
    List<ActivityLog> findByCustomerOrderByTimestampDesc(Customer customer);

    // Lấy hoạt động theo loại cụ thể (ORDER, FEEDBACK, TRANSACTION, ISSUE, ACCOUNT, ...)
    List<ActivityLog> findByCustomerAndTypeOrderByTimestampDesc(Customer customer, ActivityType type);

    // Dành riêng cho hoạt động có từ khóa như "hồ sơ", "mật khẩu" (tùy chọn nếu muốn giữ)
    List<ActivityLog> findByCustomerAndActionContainingIgnoreCaseOrderByTimestampDesc(Customer customer, String keyword);
}
