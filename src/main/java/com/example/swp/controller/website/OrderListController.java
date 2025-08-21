package com.example.swp.controller.website;

import com.example.swp.entity.Order;
import com.example.swp.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller

@RequestMapping("/SWP")
@PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
public class OrderListController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/orders")
    public String listOrders(
            @RequestParam(value = "status", required = false, defaultValue = "ALL") String status,
            @RequestParam(value = "orderId", required = false) Integer orderId,
            Model model
    ) {
        List<Order> orders;
        if (orderId != null) {
            Optional<Order> foundOrder = orderService.getOrderById(orderId);
            orders = foundOrder.map(List::of).orElse(List.of());
            // Để trạng thái filter hiện đúng:
            model.addAttribute("selectedStatus", "ALL");
        } else if ("ALL".equalsIgnoreCase(status)) {
            orders = orderService.getAllOrders();
            model.addAttribute("selectedStatus", "ALL");
        } else {
            orders = orderService.findOrdersByStatus(status);
            model.addAttribute("selectedStatus", status);
        }
        model.addAttribute("orders", orders);
        return "order-list";
    }

}