package com.example.swp.controller.website;

import com.example.swp.entity.Order;
import com.example.swp.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/SWP")
public class CustomerPaymentController {

    private final OrderService orderService;

    public CustomerPaymentController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** Pay a single order (redirects to /create-payment?orderId=...) */
    @PostMapping("/orders/{id}/pay")
    public String payOrder(@PathVariable int id, RedirectAttributes ra) {
        Optional<Order> opt = orderService.getOrderById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
            return "redirect:/SWP/booking/my-orders";
        }
        Order order = opt.get();

        if (!"APPROVED".equalsIgnoreCase(order.getStatus())) {
            ra.addFlashAttribute("error", "Đơn hàng chưa sẵn sàng thanh toán.");
            return "redirect:/SWP/booking/my-orders";
        }

        // Redirect to the VNPay creation endpoint
        return "redirect:/create-payment?orderId=" + order.getId();
    }
    @GetMapping("/orders/{id}/pay")
    public String payOrderGet(@PathVariable int id, RedirectAttributes ra) {
        return orderService.getOrderById(id)
                .map(o -> {
                    if (!"APPROVED".equalsIgnoreCase(o.getStatus())) {
                        ra.addFlashAttribute("error", "Đơn hàng chưa sẵn sàng thanh toán.");
                        return "redirect:/SWP/booking/my-orders";
                    }
                    return "redirect:/create-payment?orderId=" + o.getId();
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
                    return "redirect:/SWP/booking/my-orders";
                });
    }

}
