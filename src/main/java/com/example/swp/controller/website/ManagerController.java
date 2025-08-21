package com.example.swp.controller.website;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cloudinary.Cloudinary;
import com.example.swp.annotation.LogActivity;
import com.example.swp.dto.StorageRequest;
import com.example.swp.entity.Customer;
import com.example.swp.entity.EContract;
import com.example.swp.entity.Manager;
import com.example.swp.entity.Order;
import com.example.swp.entity.Staff;
import com.example.swp.entity.Storage;
import com.example.swp.repository.FeedbackRepository;
import com.example.swp.repository.OrderRepository;
import com.example.swp.service.CloudinaryService;
import com.example.swp.service.CustomerService;
import com.example.swp.service.EContractService;
import com.example.swp.service.OrderService;
import com.example.swp.service.StaffService;
import com.example.swp.service.StorageService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class ManagerController {

    @Autowired
    OrderService orderService;

    @Autowired
    FeedbackRepository feedbackRepository;
    @Autowired
    OrderRepository orderRepository;

    @Autowired
    StorageService storageService;

    @Autowired
    CustomerService customerService;

    @Autowired
    Cloudinary cloudinary;

    @Autowired
    CloudinaryService cloudinaryService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private EContractService eContractService;

    @GetMapping("/manager-dashboard")
    public String showDashboard(Model model, HttpSession session) {
        Manager loggedInManager = (Manager) session.getAttribute("loggedInManager");
        if (loggedInManager != null) {
            model.addAttribute("user", loggedInManager.getFullname());
            model.addAttribute("userName", loggedInManager.getEmail());
            model.addAttribute("userRole", "Manager");
        }

        // Doanh thu
        double totalRevenueAll = orderService.getTotalRevenueAll();
        double revenuePaid = orderService.getRevenuePaid();
        double revenueApproved = orderService.getRevenueApproved();

        model.addAttribute("revenueLabels", new String[]{"Tổng DT dự kiến", "DT Đã thanh toán", "DT Chờ thanh toán"});
        model.addAttribute("revenueValues", new double[]{totalRevenueAll, revenuePaid, revenueApproved});

        List<Storage> storages = storageService.getAll();
        int totalStorages = storages.size();

        List<Customer> customers = customerService.getAll();
        int totalUser = customers.size();

        List<Staff> staff = staffService.getAllStaff();
        int totalStaff = staff.size();

        List<Order> last5orders = orderService.getLast5orders();

        // Chỗ này sửa lại để không lỗi null khi chưa có doanh thu
        Double totalRevenueRaw = orderRepository.calculateTotalRevenue();
        double totalRevenue = (totalRevenueRaw != null) ? totalRevenueRaw : 0.0;

        model.addAttribute("storages", storages);
        model.addAttribute("totalStorages", totalStorages);
        model.addAttribute("customers", customers);
        model.addAttribute("totalUser", totalUser);
        model.addAttribute("staff", staff);
        model.addAttribute("totalStaff", totalStaff);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("latestOrders", last5orders);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            model.addAttribute("userName", userDetails.getUsername());
            model.addAttribute("userRole", auth.getAuthorities().iterator().next().getAuthority());
        }

        return "admin";
    }

    @GetMapping("/manager-customer-list")
    public String showUserList(Model model) {
        List<Customer> customers = customerService.getAll();
        int totalCustomers = customers.size();
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("customers", customers);
        return "manager-customer-list";
    }

    @GetMapping("/manager-all-storage")
    public String showAllStorageList(Model model, HttpSession session) {
        // Populate user info for manager taskbar
        Manager loggedInManager = (Manager) session.getAttribute("loggedInManager");
        if (loggedInManager != null) {
            model.addAttribute("user", loggedInManager.getFullname());
            model.addAttribute("userName", loggedInManager.getEmail());
            model.addAttribute("userRole", "MANAGER");
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                UserDetails userDetails = (UserDetails) auth.getPrincipal();
                model.addAttribute("userName", userDetails.getUsername());
                model.addAttribute("userRole", auth.getAuthorities().iterator().next().getAuthority());
            }
        }

        List<Storage> storages = storageService.getAll();
        model.addAttribute("storages", storages);
        return "manager-all-storage"; // Tên file HTML tương ứng
    }

    @GetMapping("/addstorage")
    public String showAddStorageForm(Model model, HttpSession session) {
        // Populate user info for taskbar
        Manager loggedInManager = (Manager) session.getAttribute("loggedInManager");
        if (loggedInManager != null) {
            model.addAttribute("user", loggedInManager.getFullname());
            model.addAttribute("userName", loggedInManager.getEmail());
            model.addAttribute("userRole", "MANAGER");
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                UserDetails userDetails = (UserDetails) auth.getPrincipal();
                model.addAttribute("userName", userDetails.getUsername());
                model.addAttribute("userRole", auth.getAuthorities().iterator().next().getAuthority());
            }
        }

        model.addAttribute("storage", new Storage());
        return "addstorage";
    }

    @LogActivity(action = "Thêm kho hàng")
    @PostMapping("/addstorage")
    public String addStorage(@ModelAttribute StorageRequest storageRequest,
            @RequestParam("image") MultipartFile file, @Valid RedirectAttributes redirectAttributes) {
        try {
            // Upload ảnh
            if (file != null && !file.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(file);
                storageRequest.setImUrl(imageUrl);
            }

            // Gọi service lưu vào DB
            storageService.createStorage(storageRequest);
            redirectAttributes.addFlashAttribute("message", "Thêm kho thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Lỗi khi thêm kho.");
        }
        return "redirect:/admin/manager-dashboard";
    }

    @GetMapping("/manager-dashboard/storages/{id}")
    public String viewStorageDetail(@PathVariable int id, Model model) {
        Optional<Storage> optionalStorage = storageService.findByID(id);
        if (optionalStorage.isPresent()) {
            model.addAttribute("storage", optionalStorage.get());
        } else {
            return "redirect:/admin/manager-dashboard";
        }
        return "manager-storagedetail";
    }

    @GetMapping("/manager-dashboard/storages/{id}/edit")
    public String showEditForm(@PathVariable("id") int id, Model model) {
        Optional<Storage> optionalStorage = storageService.findByID(id);
        if (optionalStorage.isPresent()) {
            model.addAttribute("storage", optionalStorage.get());
        } else {
            return "redirect:/admin/manager-dashboard";
        }
        return "manager-storage-edit";
    }

    @LogActivity(action = "Xoá kho")
    @PostMapping("/storages/{id}/delete")
    public String deleteStorage(@PathVariable int id,
                                @RequestParam(value = "returnUrl", required = false) String returnUrl,
                                RedirectAttributes redirectAttributes) {
        storageService.deleteStorageById(id);
        redirectAttributes.addFlashAttribute("message", "Đã xoá kho thành công!");
        if (returnUrl == null || returnUrl.isEmpty()) {
            return "redirect:/admin/storages";
        }
        return "redirect:" + returnUrl;
    }

    @LogActivity(action = "Cập nhật kho")
    @PutMapping("/manager-dashboard/storages/{id}")
    public String updateStorage(@PathVariable int id,
            RedirectAttributes redirectAttributes,
            @ModelAttribute StorageRequest storageRequest) {
        Optional<Storage> optional = storageService.findByID(id);
        if (optional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy kho!");
            return "redirect:/admin/manager-dashboard";
        }

        storageService.updateStorage(storageRequest, optional.get());
        redirectAttributes.addFlashAttribute("message", "Cập nhật thành công!");

        return "manager-storagedetail";
    }

    @PostMapping("/manager-dashboard/storages/{id}")
    public String updateStoragePost(@PathVariable int id,
            RedirectAttributes redirectAttributes,
            @ModelAttribute StorageRequest storageRequest,
            Model model) {
        Optional<Storage> optional = storageService.findByID(id);
        if (optional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy kho!");
            return "redirect:/admin/manager-dashboard";
        }
        Storage updated = storageService.updateStorage(storageRequest, optional.get());
        model.addAttribute("storage", updated);
        redirectAttributes.addFlashAttribute("message", "Cập nhật thành công!");
        return "manager-storagedetail";
    }

    //danh sách staff
    @GetMapping("/staff-list")
    public String showStaffList(
            Model model,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Staff> staffPage = staffService.getStaffsByPage(page - 1, size);

        int totalStaff = staffService.countAllStaff();

        model.addAttribute("staffPage", staffPage);
        model.addAttribute("staffs", staffPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", staffPage.getTotalPages());
        model.addAttribute("totalStaff", totalStaff);

        return "staff-list";
    }

    @GetMapping("/staff-list/detail/{id}")
    public String viewStaffDetail(@PathVariable int id, Model model) {
        Optional<Staff> optionalStaff = staffService.findById(id);
        if (optionalStaff.isPresent()) {
            model.addAttribute("staff", optionalStaff.get());
        } else {
            return "redirect:/admin/staff-list";
        }
        return "staff-detail";
    }

    @GetMapping("/staff-list/edit/{id}")
    public String showEditStaffForm(@PathVariable int id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Staff> staffOpt = staffService.findById(id);
        if (staffOpt.isPresent()) {
            model.addAttribute("staff", staffOpt.get());
            return "edit-staff";
        } else {
            redirectAttributes.addFlashAttribute("error", "Staff not found!");
            return "redirect:/admin/staff-list";
        }
    }

    @PostMapping("/staff-list/edit/{id}")
    public String editStaff(
            @PathVariable int id,
            @ModelAttribute("staff") Staff staff,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Staff> staffOpt = staffService.findById(id);
        if (staffOpt.isPresent()) {
            Staff existingStaff = staffOpt.get();
            existingStaff.setFullname(staff.getFullname());
            existingStaff.setEmail(staff.getEmail());
            existingStaff.setPhone(staff.getPhone());
            existingStaff.setIdCitizenCard(staff.getIdCitizenCard());

            staffService.save(existingStaff);
            redirectAttributes.addFlashAttribute("message", "Cập nhật staff thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy nhân viên!");
        }
        return "redirect:/admin/staff-list";
    }

    @GetMapping("/manager-inbox")
    public String managerInbox() {
        return "manager-inbox";
    }

    @GetMapping("/social-chat")
    public String socialChat() {
        return "social-chat";
    }

    @GetMapping("/manager-setting")
    public String managerSetting(Model model, HttpSession session) {
        // Lấy Manager từ session
        Manager loggedInManager = (Manager) session.getAttribute("loggedInManager");

        if (loggedInManager != null) {
            model.addAttribute("user", loggedInManager.getFullname());     // Tên người dùng
            model.addAttribute("userName", loggedInManager.getEmail());    // Email
            model.addAttribute("userRole", "MANAGER");                     // Vai trò
        } else {
            // Phòng trường hợp null, fallback từ SecurityContext
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                UserDetails userDetails = (UserDetails) auth.getPrincipal();
                model.addAttribute("userName", userDetails.getUsername());
                model.addAttribute("userRole", auth.getAuthorities().iterator().next().getAuthority());
            }
        }

        return "manager-setting";
    }

    /**
     * Hiển thị danh sách tất cả hợp đồng cho admin
     */
    @GetMapping("/contracts")
    public String showAllContracts(Model model, HttpSession session) {
        // Populate user info for manager taskbar
        Manager loggedInManager = (Manager) session.getAttribute("loggedInManager");
        if (loggedInManager != null) {
            model.addAttribute("user", loggedInManager.getFullname());
            model.addAttribute("userName", loggedInManager.getEmail());
            model.addAttribute("userRole", "MANAGER");
        }

        // Lấy tất cả hợp đồng
        List<EContract> contracts = eContractService.findAll();
        
        // Thống kê hợp đồng
        long signedContracts = contracts.stream()
                .filter(contract -> contract.getStatus().name().equals("SIGNED"))
                .count();
        long pendingContracts = contracts.stream()
                .filter(contract -> contract.getStatus().name().equals("PENDING"))
                .count();

        model.addAttribute("contracts", contracts);
        model.addAttribute("totalContracts", contracts.size());
        model.addAttribute("signedContracts", signedContracts);
        model.addAttribute("pendingContracts", pendingContracts);

        return "admin-contracts";
    }

//    @GetMapping("/manager/profile")
//    public String managerProfilePage(HttpSession session, Model model) {
//        Manager loggedInManager = (Manager) session.getAttribute("loggedInManager");
//        if (loggedInManager != null) {
//            model.addAttribute("user", loggedInManager); // -> biến 'user' trong HTML
//        }
//        return "manager-setting";
//    }
}
