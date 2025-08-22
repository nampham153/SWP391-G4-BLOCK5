package com.example.swp.controller.website;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.swp.entity.Customer;
import com.example.swp.entity.EContract;
import com.example.swp.entity.Order;
import com.example.swp.entity.Storage;
import com.example.swp.entity.Voucher;
import com.example.swp.entity.Zone;
import com.example.swp.entity.VoucherUsage;
import com.example.swp.enums.EContractStatus;
import com.example.swp.enums.VoucherStatus;
import com.example.swp.service.CustomerService;
import com.example.swp.service.EContractService;
import com.example.swp.service.OrderService;
import com.example.swp.service.StorageService;
import com.example.swp.service.VoucherService;
import com.example.swp.repository.ZoneRepository;
import com.example.swp.service.VoucherUsageService;

import jakarta.servlet.http.HttpSession;

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

    @Autowired
    private OrderService orderService;

    @Autowired
    private EContractService eContractService;

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private VoucherUsageService voucherUsageService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ZoneRepository zoneRepository;

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
            @RequestParam(value = "zoneId", required = false) Integer zoneId,
            @RequestParam(value = "preSelectedArea", required = false) Integer preSelectedArea,
            @RequestParam(value = "selectedUnitIndices", required = false) String selectedUnitIndices,
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
        if (preSelectedArea != null && preSelectedArea > 0) {
            model.addAttribute("preSelectedArea", preSelectedArea);
        }
        if (selectedUnitIndices != null && !selectedUnitIndices.isBlank()) {
            model.addAttribute("selectedUnitIndices", selectedUnitIndices);
        }

        // Zone đã chọn (nếu có)
        if (zoneId != null) {
            Optional<Zone> zoneOpt = zoneRepository.findById(zoneId);
            zoneOpt.ifPresent(z -> model.addAttribute("selectedZone", z));
            model.addAttribute("zoneId", zoneId);
        }
        // Danh sách voucher khả dụng cho khách (đang ACTIVE và đủ điểm)
        try {
            int customerPoint = customer.getPoints() != null ? customer.getPoints() : 0;
            List<Voucher> vouchers = voucherService.getAvailableVouchersForCustomer(customerPoint);
            model.addAttribute("vouchers", vouchers);
        } catch (Exception ex) {
            model.addAttribute("vouchers", java.util.Collections.emptyList());
        }

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
            @RequestParam(value = "zoneId", required = false) Integer zoneId,
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

        // Danh sách voucher khả dụng cho khách (đang ACTIVE và đủ điểm)
        try {
            int customerPoint = customer.getPoints() != null ? customer.getPoints() : 0;
            List<Voucher> vouchers = voucherService.getAvailableVouchersForCustomer(customerPoint);
            model.addAttribute("vouchers", vouchers);
        } catch (Exception ex) {
            model.addAttribute("vouchers", java.util.Collections.emptyList());
        }

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
            @RequestParam(value = "zoneId", required = false) Integer zoneId,
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

        // Kiểm tra diện tích phải là số nguyên
        if (rentalArea != Math.floor(rentalArea)) {
            redirectAttributes.addFlashAttribute("error", "Diện tích thuê phải là số nguyên, không được nhập số thập phân.");
            return "redirect:/SWP/booking/" + storageId + "/booking?startDate=" + startDate + "&endDate=" + endDate;
        }

        if (rentalArea > storage.getArea()) {
            redirectAttributes.addFlashAttribute("error",
                    "Diện tích thuê (" + rentalArea + " m²) không được vượt quá diện tích kho (" + storage.getArea()
                            + " m²).");
            return "redirect:/SWP/booking/" + storageId + "/booking?startDate=" + startDate + "&endDate=" + endDate;
        }

        try {
            // Hủy các đơn hàng cũ của khách hàng cho cùng kho này (nếu có)
            orderService.cancelExistingOrdersForCustomerAndStorage(
                customer.getId(), 
                storageId, 
                "Khách hàng đặt lại cùng kho - Hủy đơn hàng cũ tự động"
            );

            // Tính toán chi phí
            long days = ChronoUnit.DAYS.between(startDate, endDate);
            double pricePerDay = storage.getPricePerDay();
            double totalCost = days * pricePerDay * (rentalArea / storage.getArea());

            // Áp dụng voucher nếu có
            Voucher appliedVoucher = null;
            BigDecimal discountApplied = BigDecimal.ZERO;
            if (voucherId != null) {
                Optional<Voucher> voucherOpt = voucherService.getVoucherById(voucherId);
                if (voucherOpt.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Voucher không tồn tại.");
                    return "redirect:/SWP/booking/" + storageId + "/booking?startDate=" + startDate + "&endDate=" + endDate;
                }

                Voucher voucher = voucherOpt.get();
                int customerPoint = customer.getPoints() != null ? customer.getPoints() : 0;

                // Validate voucher điều kiện cơ bản
                boolean validStatus = voucher.getStatus() == VoucherStatus.ACTIVE;
                boolean withinTime = (voucher.getStartDate() == null || !voucher.getStartDate().isAfter(LocalDateTime.now()))
                        && (voucher.getEndDate() == null || voucher.getEndDate().isAfter(LocalDateTime.now()));
                boolean hasQuantity = voucher.getRemainQuantity() != null && voucher.getRemainQuantity() > 0;
                boolean enoughPoint = voucher.getRequiredPoint() == null || voucher.getRequiredPoint() <= customerPoint;

                if (!(validStatus && withinTime && hasQuantity && enoughPoint)) {
                    redirectAttributes.addFlashAttribute("error", "Voucher không còn khả dụng hoặc bạn không đủ điểm.");
                    return "redirect:/SWP/booking/" + storageId + "/booking?startDate=" + startDate + "&endDate=" + endDate;
                }

                // Tính giảm giá và cập nhật điểm/quantity
                BigDecimal original = BigDecimal.valueOf(totalCost);
                BigDecimal discount = voucher.getDiscountAmount() != null ? voucher.getDiscountAmount() : BigDecimal.ZERO;
                BigDecimal finalAmount = original.subtract(discount);
                if (finalAmount.signum() < 0) finalAmount = BigDecimal.ZERO; // không âm

                discountApplied = original.subtract(finalAmount);
                totalCost = finalAmount.doubleValue();
                appliedVoucher = voucher;

                // Trừ điểm khách nếu voucher yêu cầu điểm
                if (voucher.getRequiredPoint() != null && voucher.getRequiredPoint() > 0) {
                    int newPoints = Math.max(0, customerPoint - voucher.getRequiredPoint());
                    customer.setPoints(newPoints);
                    customerService.save(customer);
                }

                // Giảm số lượng còn lại của voucher
                if (voucher.getRemainQuantity() != null) {
                    voucher.setRemainQuantity(Math.max(0, voucher.getRemainQuantity() - 1));
                }
                voucherService.saveVoucher(voucher);
            }

            // Tạo và lưu đơn hàng vào database
            Order order = new Order();
            order.setCustomer(customer);
            order.setStartDate(startDate);
            order.setEndDate(endDate);
            order.setOrderDate(LocalDate.now());
            order.setTotalAmount(totalCost);
            order.setStatus("PENDING");
            // Gán zone nếu người dùng đã chọn
            if (zoneId != null) {
                zoneRepository.findById(zoneId).ifPresent(order::setZone);
            }
            if (appliedVoucher != null) {
                order.setVoucher(appliedVoucher);
            }

            // Lưu đơn hàng vào database
            Order savedOrder = orderService.save(order);

            // Lưu lịch sử sử dụng voucher nếu có
            if (appliedVoucher != null) {
                VoucherUsage usage = new VoucherUsage();
                usage.setCustomer(customer);
                usage.setVoucher(appliedVoucher);
                usage.setOrder(savedOrder);
                usage.setUsedAt(LocalDateTime.now());
                usage.setDiscountAmount(discountApplied);
                voucherUsageService.save(usage);
            }

            // Tạo hợp đồng cho đơn hàng
            EContract contract = eContractService.createContract(savedOrder);

            // Lưu ID đơn hàng vào session để hiển thị trong booking detail
            session.setAttribute("latestOrderId", savedOrder.getId());

            // Thông báo thành công
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đặt kho thành công! Mã đơn hàng: #" + savedOrder.getId() + 
                    ". Tổng chi phí: " + String.format("%,.0f", totalCost) + " VNĐ");

            // Xóa order token khỏi session
            session.removeAttribute("orderToken");

            // Chuyển đến trang booking detail
            return "redirect:/SWP/booking/detail";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi đặt kho: " + e.getMessage());
            return "redirect:/SWP/booking/" + storageId + "/booking?startDate=" + startDate + "&endDate=" + endDate;
        }
    }

    /**
     * Hiển thị trang booking detail sau khi đặt kho thành công
     */
    @GetMapping("/booking/detail")
    public String showBookingDetail(
            @RequestParam(value = "orderId", required = false) Integer orderId,
            Model model, 
            HttpSession session) {

        // Kiểm tra customer đã đăng nhập chưa
        Customer customer = getLoggedInCustomer(session);
        if (customer == null) {
            return "redirect:/api/login";
        }

        // Lấy ID đơn hàng từ parameter hoặc session
        Integer targetOrderId = orderId;
        if (targetOrderId == null) {
            targetOrderId = (Integer) session.getAttribute("latestOrderId");
        }
        
        if (targetOrderId == null) {
            return "redirect:/SWP/customers/my-bookings";
        }

        // Lấy đơn hàng từ database
        Optional<Order> optionalOrder = orderService.findOrderById(targetOrderId);
        if (optionalOrder.isEmpty()) {
            return "redirect:/SWP/customers/my-bookings";
        }

        Order order = optionalOrder.get();

        // Kiểm tra xem đơn hàng có thuộc về customer hiện tại không
        if (order.getCustomer().getId() != customer.getId()) {
            return "redirect:/SWP/customers/my-bookings";
        }

        // Thêm attributes vào model
        model.addAttribute("order", order);
        model.addAttribute("customer", customer);

        return "booking-detail";
    }

    /**
     * Hiển thị danh sách đơn hàng của khách hàng
     */
    @GetMapping("/customers/my-bookings")
    public String myBookings(Model model, HttpSession session) {
        
        // Kiểm tra customer đã đăng nhập chưa
        Customer customer = getLoggedInCustomer(session);
        if (customer == null) {
            return "redirect:/api/login";
        }

        // Lấy danh sách đơn hàng của khách hàng (loại trừ đơn hàng đã hủy)
        List<Order> allOrders = orderService.findOrdersByCustomer(customer);
        List<Order> orders = allOrders.stream()
                .filter(order -> !"CANCELLED".equals(order.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        
        // Tính tổng giá trị đơn hàng (chỉ tính đơn hàng không bị hủy)
        double totalAmount = orders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();

        // Kiểm tra xem tất cả hợp đồng đã được ký chưa
        boolean allContractsSigned = eContractService.areAllContractsSignedForCustomer(customer.getId());

        // Thêm attributes vào model
        model.addAttribute("orders", orders);
        model.addAttribute("customer", customer);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("allContractsSigned", allContractsSigned);

        return "my-bookings";
    }

    /**
     * Hủy đơn hàng
     */
    @PostMapping("/customers/cancel-order/{orderId}")
    public String cancelOrder(@PathVariable int orderId, 
                             HttpSession session, 
                             RedirectAttributes redirectAttributes) {
        
        // Kiểm tra customer đã đăng nhập chưa
        Customer customer = getLoggedInCustomer(session);
        if (customer == null) {
            return "redirect:/api/login";
        }

        // Lấy đơn hàng từ database
        Optional<Order> optionalOrder = orderService.findOrderById(orderId);
        if (optionalOrder.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng");
            return "redirect:/SWP/customers/my-bookings";
        }

        Order order = optionalOrder.get();

        // Kiểm tra xem đơn hàng có thuộc về customer hiện tại không
        if (order.getCustomer().getId() != customer.getId()) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền hủy đơn hàng này");
            return "redirect:/SWP/customers/my-bookings";
        }

        // Chỉ cho phép hủy đơn hàng có trạng thái PENDING
        if (!"PENDING".equals(order.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Chỉ có thể hủy đơn hàng đang chờ xử lý");
            return "redirect:/SWP/customers/my-bookings";
        }

        try {
            // Cập nhật trạng thái đơn hàng thành CANCELLED
            order.setStatus("CANCELLED");
            order.setCancelReason("Khách hàng hủy đơn");
            orderService.save(order);

            redirectAttributes.addFlashAttribute("successMessage", 
                "Đã hủy đơn hàng #" + orderId + " thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra khi hủy đơn hàng: " + e.getMessage());
        }

        return "redirect:/SWP/customers/my-bookings";
    }

    /**
     * Hiển thị trang hợp đồng thuê kho
     */
    @GetMapping("/booking/contract")
    public String showContract(
            @RequestParam("orderId") Integer orderId,
            Model model, 
            HttpSession session) {

        // Kiểm tra customer đã đăng nhập chưa
        Customer customer = getLoggedInCustomer(session);
        if (customer == null) {
            return "redirect:/api/login";
        }

        // Lấy đơn hàng từ database
        Optional<Order> optionalOrder = orderService.findOrderById(orderId);
        if (optionalOrder.isEmpty()) {
            return "redirect:/SWP/customers/my-bookings";
        }

        Order order = optionalOrder.get();

        // Kiểm tra xem đơn hàng có thuộc về customer hiện tại không
        if (order.getCustomer().getId() != customer.getId()) {
            return "redirect:/SWP/customers/my-bookings";
        }

        // Lấy hoặc tạo hợp đồng cho đơn hàng
        Optional<EContract> contractOpt = eContractService.findByOrder(order);
        EContract contract;
        if (contractOpt.isEmpty()) {
            contract = eContractService.createContract(order);
        } else {
            contract = contractOpt.get();
        }

        // Thêm attributes vào model
        model.addAttribute("order", order);
        model.addAttribute("customer", customer);
        model.addAttribute("contract", contract);

        return "view-contract";
    }

    /**
     * Xử lý ký hợp đồng
     */
    @PostMapping("/econtract/sign/{id}")
    public String signContract(@PathVariable Long id, 
                              HttpSession session, 
                              RedirectAttributes redirectAttributes) {
        
        // Kiểm tra customer đã đăng nhập chưa
        Customer customer = getLoggedInCustomer(session);
        if (customer == null) {
            return "redirect:/api/login";
        }

        try {
            // Lấy hợp đồng
            Optional<EContract> contractOpt = eContractService.findById(id);
            if (contractOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy hợp đồng");
                return "redirect:/SWP/customers/my-bookings";
            }

            EContract contract = contractOpt.get();
            
            // Kiểm tra xem hợp đồng có thuộc về customer hiện tại không
            if (contract.getOrder().getCustomer().getId() != customer.getId()) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền ký hợp đồng này");
                return "redirect:/SWP/customers/my-bookings";
            }

            // Ký hợp đồng
            EContract signedContract = eContractService.signContract(id);
            
            redirectAttributes.addFlashAttribute("successMessage", "Hợp đồng đã được ký thành công!");
            return "redirect:/SWP/booking/contract?orderId=" + signedContract.getOrder().getId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi ký hợp đồng: " + e.getMessage());
            return "redirect:/SWP/customers/my-bookings";
        }
    }

    /**
     * Hiển thị danh sách hợp đồng của khách hàng
     */
    @GetMapping("/customers/my-contracts")
    public String myContracts(Model model, HttpSession session) {
        
        // Kiểm tra customer đã đăng nhập chưa
        Customer customer = getLoggedInCustomer(session);
        if (customer == null) {
            return "redirect:/api/login";
        }

        // Lấy danh sách hợp đồng của khách hàng
        List<EContract> allContracts = eContractService.findByCustomerId(customer.getId());
        
        // Chỉ hiển thị hợp đồng từ các đơn hàng đã thanh toán (PAID) hoặc đã được duyệt (APPROVED)
        List<EContract> contracts = allContracts.stream()
                .filter(contract -> {
                    String orderStatus = contract.getOrder().getStatus();
                    return "PAID".equals(orderStatus) || "APPROVED".equals(orderStatus);
                })
                .collect(java.util.stream.Collectors.toList());
        
        // Tính toán thống kê dựa trên danh sách đã lọc
        long signedCount = contracts.stream()
                .filter(contract -> contract.getStatus().name().equals("SIGNED"))
                .count();
        long pendingCount = contracts.stream()
                .filter(contract -> contract.getStatus().name().equals("PENDING"))
                .count();
        long cancelledCount = contracts.stream()
                .filter(contract -> contract.getStatus().name().equals("CANCELLED"))
                .count();
        long pendingCancellationCount = contracts.stream()
                .filter(contract -> contract.getStatus().name().equals("PENDING_CANCELLATION"))
                .count();
        
        // Thêm attributes vào model
        model.addAttribute("contracts", contracts);
        model.addAttribute("customer", customer);
        model.addAttribute("signedCount", signedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("pendingCancellationCount", pendingCancellationCount);

        return "my-contracts";
    }

    /**
     * Hủy hợp đồng
     */
    @PostMapping("/customers/cancel-contract/{contractId}")
    public String cancelContract(@PathVariable Long contractId, 
                                HttpSession session, 
                                RedirectAttributes redirectAttributes) {
        
        System.out.println("DEBUG: Cancel contract called with ID: " + contractId);
        
        // Kiểm tra customer đã đăng nhập chưa
        Customer customer = getLoggedInCustomer(session);
        if (customer == null) {
            System.out.println("DEBUG: Customer not logged in");
            return "redirect:/api/login";
        }

        System.out.println("DEBUG: Customer ID: " + customer.getId());

        try {
            // Lấy hợp đồng
            Optional<EContract> contractOpt = eContractService.findById(contractId);
            if (contractOpt.isEmpty()) {
                System.out.println("DEBUG: Contract not found with ID: " + contractId);
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy hợp đồng");
                return "redirect:/SWP/customers/my-contracts";
            }

            EContract contract = contractOpt.get();
            System.out.println("DEBUG: Found contract: " + contract.getContractCode() + ", Status: " + contract.getStatus());
            
            // Kiểm tra xem hợp đồng có thuộc về customer hiện tại không
            if (contract.getOrder().getCustomer().getId() != customer.getId()) {
                System.out.println("DEBUG: Contract belongs to different customer");
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền hủy hợp đồng này");
                return "redirect:/SWP/customers/my-contracts";
            }

            // Kiểm tra trạng thái hợp đồng
            if (contract.getStatus() == EContractStatus.CANCELLED) {
                System.out.println("DEBUG: Contract already cancelled");
                redirectAttributes.addFlashAttribute("error", "Hợp đồng đã được hủy trước đó");
                return "redirect:/SWP/customers/my-contracts";
            }
            
            if (contract.getStatus() == EContractStatus.PENDING_CANCELLATION) {
                System.out.println("DEBUG: Contract cancellation already requested");
                redirectAttributes.addFlashAttribute("error", "Yêu cầu hủy hợp đồng đã được gửi, chờ admin xác nhận");
                return "redirect:/SWP/customers/my-contracts";
            }
            
            // Chỉ cho phép hủy hợp đồng đã ký
            if (contract.getStatus() != EContractStatus.SIGNED) {
                System.out.println("DEBUG: Contract not in SIGNED status, current status: " + contract.getStatus());
                redirectAttributes.addFlashAttribute("error", "Chỉ có thể hủy hợp đồng đã ký");
                return "redirect:/SWP/customers/my-contracts";
            }

            // Gửi yêu cầu hủy hợp đồng (chờ admin xác nhận)
            System.out.println("DEBUG: Requesting contract cancellation...");
            EContract updatedContract = eContractService.requestCancellation(contractId);
            System.out.println("DEBUG: Contract cancellation requested. New status: " + updatedContract.getStatus());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Yêu cầu hủy hợp đồng " + contract.getContractCode() + " đã được gửi. Chờ admin xác nhận!");
            
        } catch (Exception e) {
            System.out.println("DEBUG: Error cancelling contract: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra khi hủy hợp đồng: " + e.getMessage());
        }

        return "redirect:/SWP/customers/my-contracts";
    }
}
