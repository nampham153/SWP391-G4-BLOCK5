package com.example.swp.controller.website;

import com.example.swp.entity.Order;
import com.example.swp.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.*;
@Controller
@RequestMapping("/{area:admin|staff}")
public class OrderListController {

    private final OrderService orderService;
    public OrderListController(OrderService orderService) { this.orderService = orderService; }

    @GetMapping("/orders")
    @PreAuthorize("(#area == 'admin' and hasAuthority('MANAGER')) or (#area == 'staff' and hasAuthority('STAFF'))")
    public String listOrders(@PathVariable String area,
                             @RequestParam(value = "status",  defaultValue = "ALL") String status,
                             @RequestParam(value = "orderId", required = false) Integer orderId,
                             @RequestParam(value = "sort",    defaultValue = "newest") String sort,  // ← NEW
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

        // --- NEW: sort by orderDate, newest first by default ---
        Comparator<Order> cmp = Comparator
                .comparing(Order::getOrderDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Order::getId);
        if ("oldest".equalsIgnoreCase(sort)) {
            orders.sort(cmp);           // ascending (oldest → newest)
        } else {
            orders.sort(cmp.reversed()); // descending (newest → oldest)
        }

        model.addAttribute("orders", orders);
        model.addAttribute("selectedSort", sort.toLowerCase());
        model.addAttribute("base", "/" + area);
        return "order-list";
    }
}
