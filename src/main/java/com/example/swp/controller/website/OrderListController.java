package com.example.swp.controller.website;

import com.example.swp.entity.Order;
import com.example.swp.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/{area:admin|staff}")  // ⬅ replace "/SWP"
public class OrderListController {

    private final OrderService orderService;
    public OrderListController(OrderService orderService) { this.orderService = orderService; }

    @GetMapping("/orders")
    @PreAuthorize("(#area == 'admin' and hasAuthority('MANAGER')) or (#area == 'staff' and hasAuthority('STAFF'))")
    public String listOrders(@PathVariable String area,
                             @RequestParam(value = "status", required = false, defaultValue = "ALL") String status,
                             @RequestParam(value = "orderId", required = false) Integer orderId,
                             Model model) {
        List<Order> orders;
        if (orderId != null) {
            Optional<Order> foundOrder = orderService.getOrderById(orderId);
            orders = foundOrder.map(List::of).orElse(List.of());
            model.addAttribute("selectedStatus", "ALL");
        } else if ("ALL".equalsIgnoreCase(status)) {
            orders = orderService.getAllOrders();
            model.addAttribute("selectedStatus", "ALL");
        } else {
            orders = orderService.findOrdersByStatus(status);
            model.addAttribute("selectedStatus", status);
        }
        model.addAttribute("orders", orders);
        model.addAttribute("base", "/" + area); // ⬅ for building links in views
        return "order-list";
    }
}
