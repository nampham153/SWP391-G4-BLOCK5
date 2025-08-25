package com.example.swp.controller.api;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.swp.entity.Order;
import com.example.swp.service.OrderService;
import com.example.swp.service.impl.OrderServiceimpl;
import com.example.swp.service.impl.VnPayServiceimpl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final VnPayServiceimpl vnPayService;
    private final OrderServiceimpl orderServiceimpl;
    private final OrderService orderService;

    @GetMapping("/create-payment")
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
    }

    @GetMapping("/payment-return")
    public String handleVnPayReturn(@RequestParam Map<String, String> params, Model model) {
        String code = params.getOrDefault("vnp_ResponseCode", "");
        String txnRef = params.getOrDefault("vnp_TxnRef", "");
        String transNo = params.getOrDefault("vnp_TransactionNo", "");
        String orderInfo = params.getOrDefault("vnp_OrderInfo", "");
        String rawAmt = params.getOrDefault("vnp_Amount", "0");

        // VNPay sends amount in "xu" => divide by 100 to show VND
        long amountVnd = 0L;
        try {
            amountVnd = Long.parseLong(rawAmt) / 100L;
        } catch (NumberFormatException ignored) {
        }

        boolean success = "00".equals(code);

        // Update order status if success (idempotent service recommended)
        try {
            if (success) {
                int orderId = Integer.parseInt(txnRef);
                orderService.markOrderAsPaid(orderId);
            }
        } catch (Exception ignored) {

        }

        // Model for the template
        model.addAttribute("vnp_ResponseCode", code);
        model.addAttribute("vnp_TxnRef", txnRef);
        model.addAttribute("vnp_TransactionNo", transNo);
        model.addAttribute("vnp_OrderInfo", orderInfo);
        model.addAttribute("amountVnd", amountVnd);
        model.addAttribute("success", success);

        return "payment-return";
    }
}
