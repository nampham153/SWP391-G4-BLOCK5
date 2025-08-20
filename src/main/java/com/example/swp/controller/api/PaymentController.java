package com.example.swp.controller.api;

import com.example.swp.entity.Order;
import com.example.swp.service.impl.OrderServiceimpl;
import com.example.swp.service.impl.VnPayServiceimpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final VnPayServiceimpl vnPayService;
    private final OrderServiceimpl orderServiceimpl;


    @GetMapping("/create-payment")
    public ResponseEntity<?> createPayment(HttpServletRequest request,
                                           @RequestParam("orderId") int orderId,
                                           @RequestParam("amount") long amount) {
        try {
            String redirectUrl = vnPayService.createVNPayUrl(request, amount, orderId);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", redirectUrl)
                    .build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi tạo thanh toán: " + e.getMessage());
        }
    }


    @GetMapping("/payment-return")
    public String handleVnPayReturn(@RequestParam Map<String, String> params, Model model) {
        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef"); // Đây là orderId bạn đã truyền lúc đầu

        try {
            int orderId = Integer.parseInt(txnRef); // Nếu bạn chắc chắn txnRef là orderId thực

            if ("00".equals(responseCode)) {
                orderServiceimpl.markOrderAsPaid(orderId); // Đánh dấu là đã thanh toán
                model.addAttribute("message", "Thanh toán thành công!");
                return "payment-succes"; // Trả về view thành công
            } else {
                model.addAttribute("message", "Thanh toán thất bại. Mã lỗi: " + responseCode);
                return "payment-fail"; // Trả về view thất bại
            }

        } catch (NumberFormatException e) {
            model.addAttribute("message", "Lỗi xử lý orderId: " + e.getMessage());
            return "payment-error"; // View cho lỗi hệ thống
        }
    }


}
