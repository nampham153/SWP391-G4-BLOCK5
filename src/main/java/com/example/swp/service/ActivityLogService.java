package com.example.swp.service;

import com.example.swp.entity.*;

import java.util.List;

public interface ActivityLogService {

    // Ghi nhật ký hoạt động
    void logActivity(String action, String description, Customer customer,
                     Order order, StorageTransaction transaction,
                     Payment payment, Feedback feedback, Issue issue);

    // Lấy log gần nhất (có giới hạn)
    List<ActivityLog> getRecentActivities(Customer customer, int limit);

    // Các nhóm hoạt động theo loại
    List<ActivityLog> getAllByCustomer(Customer customer);

    List<ActivityLog> getByOrder(Customer customer);

    List<ActivityLog> getByFeedback(Customer customer);

    List<ActivityLog> getByTransaction(Customer customer);

    List<ActivityLog> getByIssue(Customer customer);

    List<ActivityLog> getByAccount(Customer customer); // Tài khoản: đổi mật khẩu, cập nhật hồ sơ

}