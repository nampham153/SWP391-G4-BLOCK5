package com.example.swp.controller.website;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cloudinary.Cloudinary;
import com.example.swp.dto.StorageRequest;
import com.example.swp.entity.Customer;
import com.example.swp.entity.Feedback;
import com.example.swp.entity.Order;
import com.example.swp.entity.RecentActivity;
import com.example.swp.entity.Staff;
import com.example.swp.entity.Storage;
import com.example.swp.entity.Voucher;
import com.example.swp.enums.VoucherStatus;
import com.example.swp.service.CloudinaryService;
import com.example.swp.service.CustomerService;
import com.example.swp.service.FeedbackService;
import com.example.swp.service.OrderService;
import com.example.swp.service.RecentActivityService;
import com.example.swp.service.StaffService;
import com.example.swp.service.StorageService;
import com.example.swp.service.StorageTransactionService;
import com.example.swp.service.VoucherService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/staff")
public class StaffDBoardController {

    @Autowired
    StorageService storageService;
    @Autowired
    CustomerService customerService;
    @Autowired
    Cloudinary cloudinary;
    @Autowired
    CloudinaryService cloudinaryService;
    @Autowired
    OrderService orderService;
    @Autowired
    FeedbackService feedbackService;
    @Autowired
    RecentActivityService recentActivityService;
    @Autowired
    StorageTransactionService storageTransactionService;
    @Autowired
    VoucherService voucherService;
    @Autowired
    StaffService staffService;

    @GetMapping("/staff-dashboard")
    public String showDashboard(Model model, HttpSession session) {
        // Lấy thông tin staff từ session
        Object loggedInStaffObj = session.getAttribute("loggedInStaff");
        if (!(loggedInStaffObj instanceof Staff)) {
            return "redirect:/login"; // Redirect nếu chưa đăng nhập
        }
        
        Staff loggedInStaff = (Staff) loggedInStaffObj;
        
        // Lấy danh sách kho mà staff được giao quản lý
        List<Storage> managedStorages = storageService.findByStaffId(loggedInStaff.getStaffid());
        model.addAttribute("storages", managedStorages);
        
        // Thông tin kho được quản lý
        if (!managedStorages.isEmpty()) {
            Storage managedStorage = managedStorages.get(0); // Lấy kho đầu tiên (giả sử 1 staff quản lý 1 kho)
            model.addAttribute("managedStorage", managedStorage);
            
            // Lấy tất cả đơn hàng và filter theo storage
            List<Order> allOrders = orderService.getAllOrders();
            List<Order> storageOrders = allOrders.stream()
                    .filter(order -> order.getStorage() != null && order.getStorage().getStorageid() == managedStorage.getStorageid())
                    .collect(Collectors.toList());
            model.addAttribute("orders", storageOrders);
            
            // Doanh thu từ kho được quản lý
            double storageRevenue = storageOrders.stream()
                    .filter(order -> "PAID".equals(order.getStatus()))
                    .mapToDouble(Order::getTotalAmount)
                    .sum();
            model.addAttribute("allRevenue", storageRevenue);
            model.addAttribute("revenueLabels", new String[]{"Doanh thu kho"});
            model.addAttribute("revenueValues", new double[]{storageRevenue});
            
            // Thống kê trạng thái đơn hàng cho kho này
            long paidOrderCount = storageOrders.stream().filter(o -> "PAID".equals(o.getStatus())).count();
            long pendingOrderCount = storageOrders.stream().filter(o -> "PENDING".equals(o.getStatus())).count();
            long rejectedOrderCount = storageOrders.stream().filter(o -> "REJECTED".equals(o.getStatus())).count();
            long acceptedOrderCount = storageOrders.stream().filter(o -> "APPROVED".equals(o.getStatus())).count();
            model.addAttribute("orderPaidCount", paidOrderCount);
            model.addAttribute("orderPendingCount", pendingOrderCount);
            model.addAttribute("orderRejectedCount", rejectedOrderCount);
            model.addAttribute("orderAcceptedCount", acceptedOrderCount);
            
            // Khách hàng có đơn hàng tại kho này
            List<Customer> storageCustomers = storageOrders.stream()
                    .map(Order::getCustomer)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            model.addAttribute("customers", storageCustomers);
            model.addAttribute("totalUser", storageCustomers.size());
            
            // Feedback - sử dụng tất cả feedback vì chưa có filter theo storage
            List<Feedback> allFeedbacks = feedbackService.getAllFeedbacks();
            model.addAttribute("feedbacks", allFeedbacks);
            model.addAttribute("totalFeedback", allFeedbacks.size());
            
            // Trạng thái kho
            model.addAttribute("availableStorages", managedStorage.isStatus() ? 1 : 0);
            model.addAttribute("rentedStorages", managedStorage.isStatus() ? 0 : 1);
            
        } else {
            // Nếu staff chưa được giao quản lý kho nào
            model.addAttribute("allRevenue", 0.0);
            model.addAttribute("totalUser", 0);
            model.addAttribute("totalFeedback", 0);
            model.addAttribute("orderPaidCount", 0);
            model.addAttribute("orderPendingCount", 0);
            model.addAttribute("orderRejectedCount", 0);
            model.addAttribute("orderAcceptedCount", 0);
            model.addAttribute("availableStorages", 0);
            model.addAttribute("rentedStorages", 0);
            model.addAttribute("managedStorage", null);
            
            // Dữ liệu cho biểu đồ khi chưa có kho được quản lý
            model.addAttribute("revenueLabels", new String[]{"Chưa có kho"});
            model.addAttribute("revenueValues", new double[]{0.0});
            model.addAttribute("orders", List.of());
            model.addAttribute("customers", List.of());
            model.addAttribute("feedbacks", List.of());
        }

        // Recent activities (giữ nguyên - hiển thị tất cả hoạt động)
        List<RecentActivity> activities = recentActivityService.getAllActivities();
        if (activities.size() > 6) {
            activities = activities.subList(0, 6);
        }
        model.addAttribute("recentActivities", activities);

        // Voucher (giữ nguyên - hiển thị tất cả voucher)
        List<Voucher> vouchers = voucherService.getAllVouchers();
        int totalVouchers = vouchers.size();
        List<Voucher> latestVouchers = vouchers.size() > 5 ? vouchers.subList(0, 5) : vouchers;
        model.addAttribute("totalVouchers", totalVouchers);
        model.addAttribute("latestVouchers", latestVouchers);



        // Voucher status
        long activeVoucherCount = voucherService.countByStatus(VoucherStatus.ACTIVE);
        long pausedVoucherCount = voucherService.countByStatus(VoucherStatus.INACTIVE);
        long expiredVoucherCount = voucherService.countByStatus(VoucherStatus.EXPIRED);
        model.addAttribute("activeVoucherCount", activeVoucherCount);
        model.addAttribute("pausedVoucherCount", pausedVoucherCount);
        model.addAttribute("expiredVoucherCount", expiredVoucherCount);

        return "staff-dashboard";
    }

    @GetMapping("/customer-list")
    public String showUserList(Model model) {
        List<Customer> customers = customerService.getAll();
        model.addAttribute("customers", customers);
        return "customer-list"; // Trang HTML hiển thị danh sách người dùng
    }



    @GetMapping("/storages/{id}/detail")
    public String showStorageDetail(@PathVariable int id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Storage> optionalStorage = storageService.findByID(id);
        if (optionalStorage.isPresent()) {
            model.addAttribute("storage", optionalStorage.get());
            return "staff-storage-detail"; // Tên file Thymeleaf
        } else {
            redirectAttributes.addFlashAttribute("message", "Kho không tồn tại!");
            return "redirect:/SWP/staff/staff-dashboard"; // Điều hướng về dashboard nếu không tìm thấy
        }
    }

    @GetMapping("/staff-all-storage")
    public String showAllStorageList(Model model, HttpSession session) {
        Object loggedInStaffObj = session.getAttribute("loggedInStaff");
        if (!(loggedInStaffObj instanceof Staff)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();
                Staff resolved = staffService.findByEmail(email).orElse(null);
                if (resolved != null) {
                    session.setAttribute("loggedInStaff", resolved);
                    loggedInStaffObj = resolved;
                }
            }
            if (!(loggedInStaffObj instanceof Staff)) {
                return "redirect:/api/login";
            }
        }
        List<Storage> storages;
        if (loggedInStaffObj instanceof Staff) {
            int staffId = ((Staff) loggedInStaffObj).getStaffid();
            storages = storageService.findByStaffId(staffId);
            
            // Tính toán diện tích đã thuê cho mỗi storage
            for (Storage storage : storages) {
                try {
                    double rentedArea = orderService.getTotalRentedArea(storage.getStorageid());
                    double availableArea = Math.max(0, storage.getArea() - rentedArea);
                    
                    // Thêm thông tin diện tích vào model
                    model.addAttribute("rentedArea_" + storage.getStorageid(), rentedArea);
                    model.addAttribute("availableArea_" + storage.getStorageid(), availableArea);
                } catch (Exception e) {
                    // Nếu có lỗi, sử dụng giá trị mặc định
                    model.addAttribute("rentedArea_" + storage.getStorageid(), 0.0);
                    model.addAttribute("availableArea_" + storage.getStorageid(), storage.getArea());
                }
            }
        } else {
            storages = List.of();
        }
        model.addAttribute("storages", storages);
        return "staff-all-storage";
    }

    @GetMapping("/all-recent-activity")
    public String showAllRecentActivity(Model model) {
        List<RecentActivity> recentActivities = recentActivityService.getAllActivities();
        model.addAttribute("recentActivities", recentActivities);
        return "all-recent-activity"; // Tên file Thymeleaf: all-customer-recent-activity-list.html
    }

    // Diagnostic API to verify storages assigned to logged-in staff
    @GetMapping("/api/my-storages")
    @ResponseBody
    public java.util.Map<String, Object> getMyStorages(HttpSession session) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        Object loggedInStaffObj = session.getAttribute("loggedInStaff");
        if (!(loggedInStaffObj instanceof Staff)) {
            // Try resolve from security context
            org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();
                Staff resolved = staffService.findByEmail(email).orElse(null);
                if (resolved != null) {
                    session.setAttribute("loggedInStaff", resolved);
                    loggedInStaffObj = resolved;
                }
            }
        }

        if (loggedInStaffObj instanceof Staff staff) {
            List<Storage> storages = storageService.findByStaffId(staff.getStaffid());
            java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
            for (Storage s : storages) {
                java.util.Map<String, Object> item = new java.util.HashMap<>();
                item.put("storageId", s.getStorageid());
                item.put("storageName", s.getStoragename());
                item.put("status", s.isStatus());
                item.put("pricePerDay", s.getPricePerDay());
                item.put("city", s.getCity());
                item.put("zonesCount", s.getZones() != null ? s.getZones().size() : 0);
                items.add(item);
            }
            result.put("staffId", staff.getStaffid());
            result.put("email", staff.getEmail());
            result.put("storages", items);
        } else {
            result.put("error", "NOT_LOGGED_IN_AS_STAFF");
        }

        return result;
    }

    @PostMapping("/staff-add-storage")
    public String addStorage(@ModelAttribute StorageRequest storageRequest,
            @RequestParam("image") MultipartFile file,
            @RequestParam("returnUrl") String returnUrl,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        try {
            // Upload ảnh
            if (file != null && !file.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(file);
                storageRequest.setImUrl(imageUrl);
            }

            // Gán staffid từ session nếu có
            Object loggedInStaffObj = session.getAttribute("loggedInStaff");
            if (loggedInStaffObj instanceof Staff) {
                Staff loggedInStaff = (Staff) loggedInStaffObj;
                storageRequest.setStaffid(loggedInStaff.getStaffid());
            }

            // Lưu vào DB
            storageService.createStorage(storageRequest);
            redirectAttributes.addFlashAttribute("message", "Thêm kho thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Lỗi khi thêm kho.");
        }

        // Quay lại trang trước
        return "redirect:" + returnUrl;
    }

    @PostMapping("/storages/{id}/delete")
    public String deleteStorage(@PathVariable("id") int id,
            @RequestParam(value = "returnUrl", required = false) String returnUrl,
            RedirectAttributes redirectAttributes) {
        storageService.deleteStorageById(id);
        redirectAttributes.addFlashAttribute("message", "Storage deleted successfully");
        if (returnUrl == null || returnUrl.isEmpty()) {
            return "redirect:/SWP/staff/staff-all-storage";
        }
        return "redirect:" + returnUrl;
    }

    @PostMapping("/storages/{id}/edit")
    public String editStorage(@PathVariable int id,
            @ModelAttribute Storage storage,
            RedirectAttributes redirectAttributes) {
        try {
            Optional<Storage> existingStorageOpt = storageService.findByID(id);
            if (existingStorageOpt.isPresent()) {
                Storage existingStorage = existingStorageOpt.get();

                StorageRequest storageRequest = new StorageRequest();
                storageRequest.setStoragename(storage.getStoragename());
                storageRequest.setAddress(storage.getAddress());
                storageRequest.setState(storage.getState());
                storageRequest.setCity(storage.getCity());
                storageRequest.setDescription(storage.getDescription());

                storageRequest.setArea(existingStorage.getArea());
                storageRequest.setPricePerDay(existingStorage.getPricePerDay());
                storageRequest.setStatus(existingStorage.isStatus());
                storageRequest.setImUrl(existingStorage.getImUrl());

                storageService.updateStorage(storageRequest, existingStorage);

                redirectAttributes.addFlashAttribute("message", "Cập nhật kho thành công!");
                redirectAttributes.addFlashAttribute("messageType", "success");
            } else {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy kho để cập nhật!");
                redirectAttributes.addFlashAttribute("messageType", "error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Lỗi khi cập nhật kho: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        // Redirect về trang detail của storage vừa chỉnh sửa
        return "redirect:/SWP/staff/storages/" + id + "/detail";
    }

    @PostMapping("/recent-activity/{id}/delete")
    public String deleteRecentActivity(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            recentActivityService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Xóa hoạt động thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi xóa hoạt động: " + e.getMessage());
        }
        return "redirect:/SWP/staff/all-recent-activity";
    }

    @PostMapping("/storages/{id}/toggle-status")
    public String toggleStorageStatus(@PathVariable("id") int id,
            @RequestParam(value = "returnUrl", required = false) String returnUrl,
            RedirectAttributes redirectAttributes) {
        try {
            Optional<Storage> optionalStorage = storageService.findByID(id);
            if (optionalStorage.isPresent()) {
                boolean oldStatus = optionalStorage.get().isStatus();
                storageService.toggleStatusById(id); // atomic DB update
                String statusMessage = oldStatus ? "Đã chuyển từ CÒN TRỐNG sang ĐÃ THUÊ" : "Đã chuyển từ ĐÃ THUÊ sang CÒN TRỐNG";
                redirectAttributes.addFlashAttribute("message", "Trạng thái kho đã được cập nhật: " + statusMessage);
                redirectAttributes.addFlashAttribute("messageType", "success");
            } else {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy kho để cập nhật.");
                redirectAttributes.addFlashAttribute("messageType", "error");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi khi cập nhật trạng thái kho: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        if (returnUrl == null || returnUrl.isEmpty()) {
            return "redirect:/SWP/staff/staff-all-storage";
        }
        return "redirect:" + returnUrl;
    }
}
