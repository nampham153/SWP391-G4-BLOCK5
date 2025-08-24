// src/main/java/com/example/swp/controller/website/RevenueStatisticsController.java
package com.example.swp.controller.website;

import com.example.swp.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class RevenueStatisticsController {

    @Autowired
    private OrderService orderService;

    // Serve both admin & staff routes
    @GetMapping({"/admin/statistics", "/staff/statistics"})
    public String showStatistics(Model model, HttpServletRequest request, Authentication auth) {
        double totalRevenueAll = orderService.getTotalRevenueAll();
        double revenuePaid = orderService.getRevenuePaid();
        double revenueApproved = orderService.getRevenueApproved();

        Map<String, Long> statusCounts = orderService.countOrdersByStatus();
        long totalOrders = statusCounts.values().stream().mapToLong(Long::longValue).sum();


        long paid     = statusCounts.getOrDefault("PAID",     statusCounts.getOrDefault("Paid", 0L));
        long approved = statusCounts.getOrDefault("APPROVED", statusCounts.getOrDefault("Approved", 0L));
        long pending  = statusCounts.getOrDefault("PENDING",  statusCounts.getOrDefault("Pending", 0L));
        long rejected = statusCounts.getOrDefault("REJECTED", statusCounts.getOrDefault("Rejected", 0L));


        long otherCount = Math.max(0, totalOrders - paid - approved - rejected);

        double factor = (totalOrders > 0) ? (100.0 / totalOrders) : 0.0;
        double percentPaid      = paid     * factor;
        double percentApproved  = approved * factor;
        double percentRejected  = rejected * factor;
        double percentOther     = otherCount * factor; // pending + anything else not in 3 buckets

        model.addAttribute("totalRevenueAll", totalRevenueAll);
        model.addAttribute("revenuePaid", revenuePaid);
        model.addAttribute("revenueApproved", revenueApproved);

        model.addAttribute("percentPaid", percentPaid);
        model.addAttribute("percentApproved", percentApproved);
        model.addAttribute("percentRejected", percentRejected);
        model.addAttribute("percentOther", percentOther);

        // Provide base for the template links
        String uri = request.getRequestURI();
        String base = uri.startsWith("/admin") ? "/admin" : "/staff";
        model.addAttribute("base", base);


        if (auth != null && auth.isAuthenticated()) {
            model.addAttribute("userName", auth.getName());
            model.addAttribute("userRole", auth.getAuthorities().iterator().next().getAuthority());
        }

        return "revenue-statistics";
    }


}
