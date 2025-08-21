package com.example.swp.controller.website;

import com.example.swp.entity.Customer;
import com.example.swp.entity.Notification;
import com.example.swp.service.CustomerService;
import com.example.swp.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CustomerService customerService;

    @GetMapping("/notifications")
    public String getNotifications(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        // Đúng: KHÔNG dùng .orElse(null)
        Customer customer = customerService.findByEmail(email);

        if (customer == null) {
            return "redirect:/api/login";
        }

        List<Notification> notifications = notificationService.getNotificationsForCustomer(customer);
        long unreadCount = notificationService.countUnreadNotifications(customer);

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        return "notifications";
    }

    @PostMapping("/notifications/read/{id}")
    public String markAsRead(@PathVariable("id") Long id) {
        notificationService.markAsRead(id);
        return "redirect:/notifications";
    }

    @PostMapping("/notifications/read-all")
    public String markAllAsRead() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Customer customer = customerService.findByEmail(email);
        if (customer != null) {
            notificationService.markAllAsRead(customer);
        }
        return "redirect:/notifications";
    }
}
