package com.example.swp.controller.api;

import com.example.swp.entity.ActivityLog;
import com.example.swp.entity.Customer;
import com.example.swp.service.ActivityLogService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/SWP/activity-logs") // ✅ Sửa lại cho đồng bộ
public class ActivityLogController {

    @Autowired
    private ActivityLogService activityLogService;

    @GetMapping("/me")
    public List<ActivityLog> getRecentActivitiesOfCurrentCustomer(
            HttpSession session,
            @RequestParam(defaultValue = "10") int limit
    ) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            throw new RuntimeException("Bạn chưa đăng nhập!");
        }
        return activityLogService.getRecentActivities(customer, limit);
    }
    @GetMapping("/me/order")
    public List<ActivityLog> getOrderLogs(HttpSession session) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) throw new RuntimeException("Bạn chưa đăng nhập!");
        return activityLogService.getByOrder(customer);
    }

    @GetMapping("/me/account")
    public List<ActivityLog> getAccountLogs(HttpSession session) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) throw new RuntimeException("Bạn chưa đăng nhập!");
        return activityLogService.getByAccount(customer);
    }

}
