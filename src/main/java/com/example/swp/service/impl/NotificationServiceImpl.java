package com.example.swp.service.impl;

import com.example.swp.entity.Notification;
import com.example.swp.entity.Customer;
import com.example.swp.repository.NotificationRepository;
import com.example.swp.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public List<Notification> getNotificationsForCustomer(Customer customer) {
        return notificationRepository.findByCustomerOrderByCreatedAtDesc(customer);
    }

    @Override
    public long countUnreadNotifications(Customer customer) {
        return notificationRepository.countByCustomerAndIsReadFalse(customer);
    }

    @Override
    public void markAsRead(Long id) {
        Notification noti = notificationRepository.findById(id).orElse(null);
        if (noti != null && !noti.isRead()) {
            noti.setRead(true);
            notificationRepository.save(noti);
        }
    }

    @Override
    public void markAllAsRead(Customer customer) {
        List<Notification> notis = notificationRepository.findByCustomerOrderByCreatedAtDesc(customer);
        for (Notification noti : notis) {
            if (!noti.isRead()) {
                noti.setRead(true); // Sửa lại ở đây
            }
        }
        notificationRepository.saveAll(notis);
    }

    @Override
    public void createNotification(String message, Customer customer) {
        Notification noti = Notification.builder()
                .message(message)
                .isRead(false)
                .customer(customer)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(noti);
    }
}
