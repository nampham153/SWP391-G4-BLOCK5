package com.example.swp.controller.website;

import com.example.swp.entity.Order;
import com.example.swp.entity.Storage;
import com.example.swp.repository.StorageTransactionRepository;
import com.example.swp.service.EmailService;
import com.example.swp.service.OrderService;
import com.example.swp.service.StorageService;
import com.example.swp.service.VNPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/SWP")
public class OrderDetailController {

    @Autowired
    private StorageTransactionRepository storageTransactionRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private EmailService emailService;

    @Autowired
    @Qualifier("vnPayServiceimpl")
    private VNPayService vnPayService;

    @Autowired
    private StorageService storageService;


    @GetMapping("/orders/{id}")
    public String viewOrderDetail(@PathVariable int id, Model model) {
        Optional<Order> optionalOrder = orderService.getOrderById(id);
        if (optionalOrder.isPresent()) {
            model.addAttribute("order", optionalOrder.get());
        } else {
            // Xử lý nếu không tìm thấy đơn hàng
            return "redirect:/SWP/orders"; // quay lại danh sách
        }
        return "order-detail";
    }

    @PostMapping("/orders/{id}/approve")
    public String approveOrder(@PathVariable int id, RedirectAttributes redirectAttributes) {
        Optional<Order> optionalOrder = orderService.getOrderById(id);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            // Cập nhật trạng thái đơn hàng
            order.setStatus("Approved");
            orderService.save(order);




            // Gửi email xác nhận cho khách hàng
            String customerEmail = order.getCustomer().getEmail();
            String warehouseInfo = order.getStorage().getStoragename();
            double totalAmount = order.getTotalAmount();
            String paymentUrl = "http://localhost:8080/create-payment?orderId=" + order.getId() + "&amount=" + (long) totalAmount;

            String subject = "Xác nhận đơn hàng #" + id + " đã được duyệt";
            String emailBody = "Kính gửi quý khách," +
                    "Đơn hàng #" + order.getId() + " của bạn đã được duyệt." +
                    "Tổng tiền:" + (int) totalAmount + " VND" +
                    "Vui lòng thanh toán qua link dưới đây:" +
                    "\"" + paymentUrl + "\">Thanh toán ngay" +
                    "Xin cảm ơn!";


            emailService.sendEmail(customerEmail, subject, emailBody);
            // 1. Cập nhật trạng thái kho
            Storage storage = order.getStorage();
            if (storage != null) {
                storage.setStatus(false); // đánh dấu là đã thuê
                // Gọi hàm save từ service
                storageService.save(storage);
            }


            redirectAttributes.addFlashAttribute("message", "Đã duyệt đơn hàng #" + id + " và gửi email xác nhận.");
            return "redirect:/SWP/orders/{id}";
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
            return "redirect:/SWP/orders";
        }
    }

//    @PostMapping("/orders/{id}/approve")
//    public String approveOrder(@PathVariable int id, RedirectAttributes redirectAttributes) {
//        Optional<Order> optionalOrder = orderService.getOrderById(id);
//        if (optionalOrder.isPresent()) {
//            Order order = optionalOrder.get();
//
//            // Cập nhật trạng thái đơn hàng
//            order.setStatus("Approved");
//            orderService.save(order);
//
//            // Lấy thông tin khách hàng
//            String customerEmail = order.getCustomer().getEmail();
//            String warehouseInfo = order.getStorage().getStoragename();
//            double totalAmount = order.getTotalAmount();

//            // Tạo URL thanh toán (giả lập VNPay hoặc link thanh toán thật nếu bạn có tích hợp)
//            String paymentUrl = vnPayService.createVnpayUrl(
//                    String.valueOf(order.getId()),
//                    (long) order.getTotalAmount()*100, // VNPay yêu cầu số tiền là integer (đơn vị là đồng)
//                    "http://localhost:8080/payment-return"
//            );
//
//            // Tiêu đề và nội dung email
//            String subject = "Xác nhận đơn hàng #" + id + " đã được duyệt";
//            String body = "<p>Kính gửi khách hàng,</p>" +
//                    "<p>Đơn hàng #" + id + " của bạn đã được duyệt thành công.</p>" +
//                    "<p>Kho xuất hàng: <strong>" + warehouseInfo + "</strong></p>" +
//                    "<p>Tổng số tiền phải thanh toán: <strong>" + totalAmount + " VND</strong></p>" +
//                    "<p>Vui lòng quét mã QR dưới đây để thanh toán:</p>" +
//                    "<img src='cid:qr-code'>" +
//                    "<p>Cảm ơn bạn đã mua hàng!</p>";
//
//            // Gửi email với QR code
//            emailService.sendEmailWithQR(customerEmail, subject, body, paymentUrl);
//
//            redirectAttributes.addFlashAttribute("message", "Đã duyệt đơn hàng #" + id + " và gửi email xác nhận.");
//            return "redirect:/SWP/orders/{id}";
//        } else {
//            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
//            return "redirect:/SWP/orders";
//        }
//    }




    @PostMapping("/orders/{id}/reject")
    public String rejectOrder(@PathVariable int id, RedirectAttributes redirectAttributes) {
        Optional<Order> optionalOrder = orderService.getOrderById(id);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setStatus("Rejected");
            orderService.save(order);

            redirectAttributes.addFlashAttribute("message", "Đã từ chối đơn hàng #" + id + " thành công.");
            return "redirect:/SWP/orders/{id}";  // redirect về trang chi tiết đơn hàng
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
            return "redirect:/SWP/orders";  // fallback: quay lại danh sách
        }
    }



}