package com.example.swp.service;

import com.example.swp.entity.Notification;
import com.example.swp.entity.Customer;

import java.util.List;

public interface NotificationService {
    List<Notification> getNotificationsForCustomer(Customer customer);

    long countUnreadNotifications(Customer customer);

    void markAsRead(Long id);

    void markAllAsRead(Customer customer);

    void createNotification(String message, Customer customer);
}
