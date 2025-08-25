package com.example.swp.controller.api;

import com.example.swp.entity.Order;
import com.example.swp.service.OrderService;
import com.example.swp.service.VNPayService;
import com.example.swp.service.impl.OrderServiceimpl;
import com.example.swp.service.impl.VnPayServiceimpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.swp.entity.Order;
import com.example.swp.service.OrderService;
import com.example.swp.service.impl.OrderServiceimpl;
import com.example.swp.service.impl.VnPayServiceimpl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

// PaymentController.java
@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayService vnPayService;
    private final OrderService orderService;

    @GetMapping("/create-payment")

    public String createPayment(HttpServletRequest request,
            @RequestParam("orderId") int orderId,
            RedirectAttributes ra) {
        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order không tồn tại"));
        if (!"APPROVED".equalsIgnoreCase(order.getStatus())) {
            ra.addFlashAttribute("error", "Đơn hàng chưa sẵn sàng thanh toán.");
            return "redirect:/SWP/booking/my-orders";
    

    public ResponseEntity<?> createPayment(HttpServletRequest request,
            @RequestParam("orderId") int orderId) {
        try {
            Order order = orderServiceimpl.getOrderById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order không tồn tại"));

            if (!"APPROVED".equalsIgnoreCase(order.getStatus())) {
                return ResponseEntity.badRequest().body("Đơn hàng chưa sẵn sàng thanh toán");
            }

            // VNPay thường yêu cầu số tiền là integer và gửi lên theo *100 (vnp_Amount).
            // Nếu vnPayService.createVNPayUrl() ĐÃ nhân *100 bên trong => chỉ cần làm tròn VND tại đây.
            // Nếu CHƯA nhân *100 bên trong => bạn cần nhân *100 ở đây.
            long amountVnd = Math.round(order.getTotalAmount());

            String redirectUrl = vnPayService.createVNPayUrl(request, amountVnd, orderId);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", redirectUrl)
                    .build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi tạo thanh toán: " + e.getMessage());
        }
        long amountVnd = Math.round(order.getTotalAmount()); // plain VND
        String payUrl = vnPayService.createVNPayUrl(request, amountVnd, order.getId());
        return "redirect:" + payUrl;
    }

    @GetMapping("/payment-return")
    public String handleVnPayReturn(@RequestParam Map<String, String> params, Model model) {

        String code = params.getOrDefault("vnp_ResponseCode", "");
        String txnRef = params.getOrDefault("vnp_TxnRef", "0");

        String code = params.getOrDefault("vnp_ResponseCode", "");
        String txnRef = params.getOrDefault("vnp_TxnRef", "");
        String transNo = params.getOrDefault("vnp_TransactionNo", "");
        String orderInfo = params.getOrDefault("vnp_OrderInfo", "");

        String rawAmt = params.getOrDefault("vnp_Amount", "0");

        long amountVnd = 0L;
        try {
            amountVnd = Long.parseLong(rawAmt) / 100L;
        } catch (NumberFormatException ignored) {
        }

        boolean success = false;
        String error = null;

        if ("00".equals(code)) {
            try {
                int orderId = Integer.parseInt(txnRef);
                orderService.markOrderAsPaid(orderId); // throws if invalid
                success = true;
            } catch (Exception ex) {
                error = "Thanh toán thành công tại VNPay nhưng cập nhật đơn hàng thất bại: " + ex.getMessage();
            }
        } else {
            error = "VNPay từ chối giao dịch (code=" + code + ").";
        }

        model.addAttribute("success", success);
        model.addAttribute("error", error);
        model.addAttribute("amountVnd", amountVnd);
        model.addAttribute("vnp_TxnRef", txnRef);
        model.addAttribute("vnp_TransactionNo", params.get("vnp_TransactionNo"));
        model.addAttribute("vnp_OrderInfo", params.get("vnp_OrderInfo"));
        return "payment-return";
    }
}
