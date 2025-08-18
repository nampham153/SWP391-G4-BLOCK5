package com.example.swp.controller.website;

import com.example.swp.entity.Storage;
import com.example.swp.entity.Customer;
import com.example.swp.service.StorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

// Comment: Các import khác tạm thời ẩn
/*
import com.example.swp.entity.*;
import com.example.swp.service.*;
import java.util.List;
*/

@Controller
@RequestMapping("/SWP")
public class BookingController {

    @Autowired
    private StorageService storageService;

    /**
     * Helper method để kiểm tra và lấy customer từ session
     */
    private Customer getLoggedInCustomer(HttpSession session) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        System.out.println("DEBUG - Session ID: " + session.getId());
        System.out.println("DEBUG - Customer from session: " + (customer != null ? customer.getEmail() : "null"));
        System.out.println("DEBUG - Email from session: " + session.getAttribute("email"));
        System.out.println("DEBUG - All session attributes:");
        session.getAttributeNames().asIterator()
                .forEachRemaining(name -> System.out.println("  " + name + " = " + session.getAttribute(name)));
        return customer;
    }

    /**
     * Test endpoint để kiểm tra session
     */
    @GetMapping("/test-session")
    @ResponseBody
    public String testSession(HttpSession session) {
        Customer customer = getLoggedInCustomer(session);
        return "Customer: " + (customer != null ? customer.getEmail() : "null");
    }

    /**
     * Test page để kiểm tra session
     */
    @GetMapping("/session-test")
    public String sessionTestPage(HttpSession session, Model model) {
        Customer customer = getLoggedInCustomer(session);
        model.addAttribute("customer", customer);
        return "session-test";
    }

    // Comment: Các service khác tạm thời ẩn vì chưa implement đầy đủ
    /*
     * @Autowired
     * private VoucherService voucherService;
     * 
     * @Autowired
     * private OrderService orderService;
     * 
     * @Autowired
     * private CustomerService customerService;
     */

    /**
     * Xử lý POST request từ modal chọn ngày và chuyển đến form booking
     */
    @PostMapping("/booking/{storageId}/booking")
    public String processDateSelection(@PathVariable int storageId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Kiểm tra customer đã đăng nhập chưa
        Customer customer = getLoggedInCustomer(session);
        if (customer == null) {
            redirectAttributes.addFlashAttribute("error",
                    "Bạn cần đăng nhập bằng tài khoản khách hàng để đặt thuê kho");
            return "redirect:/api/login";
        }

        // Kiểm tra storage có tồn tại không
        Optional<Storage> optionalStorage = storageService.findByID(storageId);
        if (optionalStorage.isEmpty()) {
            return "redirect:/SWP/storages";
        }

        Storage storage = optionalStorage.get();

        // Kiểm tra ngày hợp lệ
        if (!endDate.isAfter(startDate)) {
            redirectAttributes.addFlashAttribute("error", "Ngày kết thúc phải sau ngày bắt đầu");
            return "redirect:/SWP/storages/" + storageId;
        }

        // Kiểm tra kho có còn trống không
        if (!storage.isStatus()) {
            redirectAttributes.addFlashAttribute("error", "Kho này hiện đang được thuê");
            return "redirect:/SWP/storages/" + storageId;
        }

        // Tính diện tích còn lại (giả sử toàn bộ diện tích đều có thể thuê)
        double remainArea = storage.getArea();

        // Tạo order token để bảo mật
        String orderToken = UUID.randomUUID().toString();
        session.setAttribute("orderToken", orderToken);

        // Thêm attributes vào model
        model.addAttribute("storage", storage);
        model.addAttribute("customer", customer);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("remainArea", remainArea);
        model.addAttribute("orderToken", orderToken);

        return "booking-form";
    }

    /**
     * Hiển thị form booking với thông tin kho và khách hàng (GET method - để
     * backward compatibility)
     */
    @GetMapping("/booking/{storageId}/booking")
    public String showBookingForm(@PathVariable int storageId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model,
            HttpSession session) {

        // Kiểm tra customer đã đăng nhập chưa
        Customer customer = getLoggedInCustomer(session);
        if (customer == null) {
            return "redirect:/api/login?error=Bạn cần đăng nhập bằng tài khoản khách hàng để đặt thuê kho";
        }

        // Kiểm tra storage có tồn tại không
        Optional<Storage> optionalStorage = storageService.findByID(storageId);
        if (optionalStorage.isEmpty()) {
            return "redirect:/SWP/storages";
        }

        Storage storage = optionalStorage.get();

        // Kiểm tra ngày hợp lệ
        if (!endDate.isAfter(startDate)) {
            return "redirect:/SWP/storages/" + storageId + "?error=Ngày kết thúc phải sau ngày bắt đầu";
        }

        // Comment: Voucher functionality tạm thời ẩn
        /*
         * List<Voucher> vouchers = voucherService.getAllActiveVouchers();
         * model.addAttribute("vouchers", vouchers);
         */

        // Tính diện tích còn lại (giả sử toàn bộ diện tích đều có thể thuê)
        double remainArea = storage.getArea();

        // Tạo order token để bảo mật
        String orderToken = UUID.randomUUID().toString();
        session.setAttribute("orderToken", orderToken);

        // Thêm attributes vào model
        model.addAttribute("storage", storage);
        model.addAttribute("customer", customer);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("remainArea", remainArea);
        model.addAttribute("orderToken", orderToken);

        return "booking-form";
    }

    /**
     * Xử lý submit form booking
     */
    @PostMapping("/booking/{storageId}/booking/save")
    public String processBooking(@PathVariable int storageId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam("rentalArea") double rentalArea,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("id_citizen") String idCitizen,
            @RequestParam(value = "voucherId", required = false) Integer voucherId,
            @RequestParam("orderToken") String orderToken,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Kiểm tra customer đã đăng nhập chưa
        Customer customer = getLoggedInCustomer(session);
        if (customer == null) {
            redirectAttributes.addFlashAttribute("error",
                    "Bạn cần đăng nhập bằng tài khoản khách hàng để đặt thuê kho");
            return "redirect:/api/login";
        }

        // Kiểm tra order token để bảo mật
        String sessionToken = (String) session.getAttribute("orderToken");
        if (sessionToken == null || !sessionToken.equals(orderToken)) {
            redirectAttributes.addFlashAttribute("error", "Phiên làm việc không hợp lệ. Vui lòng thử lại.");
            return "redirect:/SWP/storages/" + storageId;
        }

        // Kiểm tra storage
        Optional<Storage> optionalStorage = storageService.findByID(storageId);
        if (optionalStorage.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy kho.");
            return "redirect:/SWP/storages";
        }

        Storage storage = optionalStorage.get();

        // Validate dữ liệu
        if (!endDate.isAfter(startDate)) {
            redirectAttributes.addFlashAttribute("error", "Ngày kết thúc phải sau ngày bắt đầu.");
            return "redirect:/SWP/booking/" + storageId + "/booking?startDate=" + startDate + "&endDate=" + endDate;
        }

        if (rentalArea <= 0) {
            redirectAttributes.addFlashAttribute("error", "Diện tích thuê phải lớn hơn 0.");
            return "redirect:/SWP/booking/" + storageId + "/booking?startDate=" + startDate + "&endDate=" + endDate;
        }

        if (rentalArea > storage.getArea()) {
            redirectAttributes.addFlashAttribute("error",
                    "Diện tích thuê (" + rentalArea + " m²) không được vượt quá diện tích kho (" + storage.getArea()
                            + " m²).");
            return "redirect:/SWP/booking/" + storageId + "/booking?startDate=" + startDate + "&endDate=" + endDate;
        }

        try {
            // Tính toán chi phí
            long days = ChronoUnit.DAYS.between(startDate, endDate);
            double pricePerDay = storage.getPricePerDay();
            double totalCost = days * pricePerDay * (rentalArea / storage.getArea());

            // Comment: Voucher processing tạm thời ẩn
            /*
             * double discountAmount = 0;
             * if (voucherId != null) {
             * Optional<Voucher> voucherOpt = voucherService.getVoucherById(voucherId);
             * if (voucherOpt.isPresent()) {
             * Voucher voucher = voucherOpt.get();
             * discountAmount = voucher.getDiscountAmount();
             * totalCost = Math.max(0, totalCost - discountAmount);
             * }
             * }
             */

            // Comment: Order creation tạm thời ẩn vì Order entity chưa có đủ fields
            /*
             * Order order = new Order();
             * order.setStorage(storage);
             * order.setStartDate(startDate);
             * order.setEndDate(endDate);
             * order.setOrderDate(LocalDate.now());
             * order.setTotalAmount(totalCost);
             * order.setStatus("PENDING");
             * order.setCustomerName(name);
             * order.setCustomerEmail(email);
             * order.setCustomerPhone(phone);
             * order.setRentalArea(rentalArea);
             * 
             * orderService.save(order);
             * 
             * redirectAttributes.addFlashAttribute("successMessage",
             * "Đặt kho thành công! Mã đơn hàng: #" + order.getId() +
             * ". Tổng chi phí: " + String.format("%,.0f", totalCost) + " VNĐ");
             */

            // Tạm thời chỉ hiển thị thông báo thành công
            redirectAttributes.addFlashAttribute("successMessage",
                    "Thông tin đặt kho đã được ghi nhận! " +
                            "Tên: " + name + ", Email: " + email + ", Phone: " + phone +
                            ", Diện tích: " + rentalArea + " m², " +
                            "Từ " + startDate + " đến " + endDate +
                            ". Tổng chi phí: " + String.format("%,.0f", totalCost) + " VNĐ");

            // Xóa order token khỏi session
            session.removeAttribute("orderToken");

            return "redirect:/SWP/storages/" + storageId;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi đặt kho: " + e.getMessage());
            return "redirect:/SWP/booking/" + storageId + "/booking?startDate=" + startDate + "&endDate=" + endDate;
        }
    }
}
