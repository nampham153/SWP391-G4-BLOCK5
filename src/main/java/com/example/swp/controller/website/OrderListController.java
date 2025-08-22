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

    public OrderListController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    @PreAuthorize("(#area == 'admin' and hasAuthority('MANAGER')) or (#area == 'staff' and hasAuthority('STAFF'))")
    public String listOrders(@PathVariable String area,
                             @RequestParam(value = "status", defaultValue = "ALL") String status,
                             @RequestParam(value = "orderId", required = false) Integer orderId,
                             @RequestParam(value = "sort", defaultValue = "newest") String sort,
                             Model model) {

        List<Order> orders;
        if (orderId != null) {
            orders = orderService.getOrderById(orderId)
                    .map(Collections::singletonList)     // immutable is fine, we won't mutate
                    .orElseGet(Collections::emptyList);
            model.addAttribute("selectedStatus", "ALL");
        } else if ("ALL".equalsIgnoreCase(status)) {
            orders = orderService.getAllOrders();
            model.addAttribute("selectedStatus", "ALL");
        } else {
            orders = orderService.findOrdersByStatus(status);
            model.addAttribute("selectedStatus", status);
        }

        Comparator<Order> cmp = Comparator
                .comparing(Order::getOrderDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Order::getId);

        List<Order> sorted = orders.stream()
                .sorted("oldest".equalsIgnoreCase(sort) ? cmp : cmp.reversed())
                .toList(); // Java 16+; for older, use .collect(Collectors.toList())

        model.addAttribute("orders", sorted);
        model.addAttribute("selectedSort", sort.toLowerCase());
        model.addAttribute("base", "/" + area);
        return "order-list";
    }
}
