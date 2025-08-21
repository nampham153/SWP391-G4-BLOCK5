package com.example.swp.controller.website;

import com.example.swp.entity.Order;
import com.example.swp.service.EmailService;
import com.example.swp.service.OrderService;
import com.example.swp.service.StorageService;
import com.example.swp.service.VNPayService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/{area:admin|staff}")  // ⬅ replace "/SWP"
public class OrderDetailController {

    private final OrderService orderService;
    private final EmailService emailService;
    private final VNPayService vnPayService;
    private final StorageService storageService;

    public OrderDetailController(OrderService orderService,
                                 EmailService emailService,
                                 @Qualifier("vnPayServiceimpl") VNPayService vnPayService,
                                 StorageService storageService) {
        this.orderService = orderService;
        this.emailService = emailService;
        this.vnPayService = vnPayService;
        this.storageService = storageService;
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("(#area == 'admin' and hasAuthority('MANAGER')) or (#area == 'staff' and hasAuthority('STAFF'))")
    public String viewOrderDetail(@PathVariable String area, @PathVariable int id, Model model){
        Optional<Order> optionalOrder = orderService.getOrderById(id);
        if (optionalOrder.isEmpty()) {
            return "redirect:/" + area + "/orders";
        }
        model.addAttribute("order", optionalOrder.get());
        model.addAttribute("base", "/" + area); // ⬅ for links / button visibility
        return "order-detail";
    }

    @PostMapping("/orders/{id}/approve")
    @PreAuthorize("(#area == 'admin' and hasAuthority('MANAGER')) or (#area == 'staff' and hasAuthority('STAFF'))")
    public String approveOrder(@PathVariable String area, @PathVariable int id, RedirectAttributes ra) {
        var opt = orderService.getOrderById(id);
        if (opt.isEmpty()) { ra.addFlashAttribute("error","Không tìm thấy đơn hàng."); return "redirect:/"+area+"/orders"; }
        var order = opt.get();
        order.setStatus("APPROVED");
        orderService.save(order);

        ra.addFlashAttribute("message","Đã duyệt đơn hàng #"+id+".");
        return "redirect:/"+area+"/orders/{id}";
    }

    @PostMapping("/orders/{id}/reject")
    @PreAuthorize("(#area == 'admin' and hasAuthority('MANAGER')) or (#area == 'staff' and hasAuthority('STAFF'))")
    public String rejectOrder(@PathVariable String area, @PathVariable int id, RedirectAttributes ra) {
        var opt = orderService.getOrderById(id);
        if (opt.isEmpty()) { ra.addFlashAttribute("error","Không tìm thấy đơn hàng."); return "redirect:/"+area+"/orders"; }
        var order = opt.get();
        order.setStatus("REJECTED");
        orderService.save(order);
        ra.addFlashAttribute("message","Đã từ chối đơn hàng #"+id+".");
        return "redirect:/"+area+"/orders/{id}";
    }
}
