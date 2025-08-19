package com.example.swp.controller.website;

import com.cloudinary.Cloudinary;
import com.example.swp.enums.VoucherStatus;

import com.example.swp.dto.StorageRequest;
import com.example.swp.entity.*;
import com.example.swp.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/SWP/staff")
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

    @GetMapping("/staff-dashboard")
    public String showDashboard(Model model) {
        // Doanh thu
        double totalRevenueAll = orderService.getTotalRevenueAll();
        double revenuePaid = orderService.getRevenuePaid();
        double revenueApproved = orderService.getRevenueApproved();

        model.addAttribute("allRevenue", totalRevenueAll);
        model.addAttribute("revenueLabels", new String[]{"Tổng DT dự kiến", "DT Đã thanh toán", "DT Chờ thanh toán"});
        model.addAttribute("revenueValues", new double[]{totalRevenueAll, revenuePaid, revenueApproved});

        // Danh sách kho
        List<Storage> storages = storageService.getAll();
        int totalStorages = storages.size();
        model.addAttribute("storages", storages);
        model.addAttribute("totalStorages", totalStorages);

        // Danh sách khách hàng
        List<Customer> customers = customerService.getAll();
        model.addAttribute("customers", customers);
        model.addAttribute("totalUser", customers.size());

        // Feedback
        List<Feedback> feedbacks = feedbackService.getAllFeedbacks();
        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("totalFeedback", feedbacks.size());

        // Order
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);

        // Recent activities
        List<RecentActivity> activities = recentActivityService.getAllActivities();
        if (activities.size() > 6) {
            activities = activities.subList(0, 6);
        }
        model.addAttribute("recentActivities", activities);

        // Voucher
        List<Voucher> vouchers = voucherService.getAllVouchers();
        int totalVouchers = vouchers.size();
        List<Voucher> latestVouchers = vouchers.size() > 5 ? vouchers.subList(0, 5) : vouchers;
        model.addAttribute("totalVouchers", totalVouchers);
        model.addAttribute("latestVouchers", latestVouchers);

        // ===== Thống kê số lượng theo trạng thái =====
        // Storage status
        long countAvailableStorages = storages.stream().filter(Storage::isStatus).count();
        long countRentedStorages = storages.stream().filter(s -> !s.isStatus()).count();
        model.addAttribute("availableStorages", countAvailableStorages);
        model.addAttribute("rentedStorages", countRentedStorages);

        // Order status
        var orderStatusMap = orderService.countOrdersByStatus();
        long paidOrderCount = orderStatusMap.getOrDefault("PAID", 0L);
        long pendingOrderCount = orderStatusMap.getOrDefault("PENDING", 0L);
        long rejectedOrderCount = orderStatusMap.getOrDefault("REJECTED", 0L);
        long acceptedOrderCount = orderStatusMap.getOrDefault("APPROVED", 0L);
        model.addAttribute("orderPaidCount", paidOrderCount);
        model.addAttribute("orderPendingCount", pendingOrderCount);
        model.addAttribute("orderRejectedCount", rejectedOrderCount);
        model.addAttribute("orderAcceptedCount", acceptedOrderCount);

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


    @GetMapping("/staff-add-storage")
    public String showAddStorageForm(Model model) {
        model.addAttribute("storage", new Storage());
        return "/staff-add-storage"; // Trang HTML chứa form
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
    public String showAllStorageList(Model model) {
        List<Storage> storages = storageService.getAll();
        model.addAttribute("storages", storages);
        return "staff-all-storage"; // Tên file HTML tương ứng
    }

    @GetMapping("/all-recent-activity")
    public String showAllRecentActivity(Model model) {
        List<RecentActivity> recentActivities = recentActivityService.getAllActivities();
        model.addAttribute("recentActivities", recentActivities);
        return "all-recent-activity"; // Tên file Thymeleaf: all-customer-recent-activity-list.html
    }

    @PostMapping("/staff-add-storage")
    public String addStorage(@ModelAttribute StorageRequest storageRequest,
                             @RequestParam("image") MultipartFile file,
                             @RequestParam("returnUrl") String returnUrl,
                             RedirectAttributes redirectAttributes) {
        try {
            // Upload ảnh
            if (file != null && !file.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(file);
                storageRequest.setImUrl(imageUrl);
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
        Optional<Storage> optionalStorage = storageService.findByID(id);
        if (optionalStorage.isPresent()) {
            Storage storage = optionalStorage.get();
            storage.setStatus(!storage.isStatus());
            storageService.save(storage);
            redirectAttributes.addFlashAttribute("message", "Trạng thái kho đã được cập nhật.");
        } else {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy kho để cập nhật.");
        }

        if (returnUrl == null || returnUrl.isEmpty()) {
            return "redirect:/SWP/staff/staff-all-storage";
        }
        return "redirect:" + returnUrl;
    }
}