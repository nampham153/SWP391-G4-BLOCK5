package com.example.swp.repository;

import com.example.swp.entity.Notification;
import com.example.swp.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByCustomerOrderByCreatedAtDesc(Customer customer);
    Long countByCustomerAndIsReadFalse(Customer customer); // Đúng với entity
}
