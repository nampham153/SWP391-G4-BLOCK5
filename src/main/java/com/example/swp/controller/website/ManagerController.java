package com.example.swp.controller.website;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.swp.enums.RoleName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
import com.example.swp.dto.ZoneRequest;
import com.example.swp.entity.Customer;
import com.example.swp.entity.EContract;
import com.example.swp.entity.Manager;
import com.example.swp.entity.Order;
import com.example.swp.entity.Staff;
import com.example.swp.entity.Storage;
import com.example.swp.entity.Zone;
import com.example.swp.enums.RoleName;
import com.example.swp.enums.ZoneStatus;
import com.example.swp.repository.FeedbackRepository;
import com.example.swp.repository.OrderRepository;
import com.example.swp.service.CloudinaryService;
import com.example.swp.service.CustomerService;
import com.example.swp.service.EContractService;
import com.example.swp.service.OrderService;
import com.example.swp.service.StaffService;
import com.example.swp.service.StorageService;
import com.example.swp.service.ZoneService;

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

    @Autowired
    private ZoneService zoneService;

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

        // Tạo Map để chứa số lượng ô 50m² cho mỗi storage
        Map<Integer, Integer> gridCountMap = new HashMap<>();
        for (Storage storage : storages) {
            // Tính số ô 50m² dựa trên diện tích kho
            int gridCount = (int) Math.floor(storage.getArea() / 50.0);
            gridCountMap.put(storage.getStorageid(), gridCount);
        }
        model.addAttribute("gridCountMap", gridCountMap);

        return "manager-all-storage"; // Tên file HTML tương ứng
    }

    // Alias route to satisfy redirect to /admin/manager-dashboard/storages/
    @GetMapping({"/manager-dashboard/storages", "/manager-dashboard/storages/"})
    public String redirectStoragesList() {
        return "redirect:/admin/manager-all-storage";
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
            @RequestParam(value = "image", required = false) MultipartFile file,
            @Valid RedirectAttributes redirectAttributes) {
        try {
            // Upload ảnh
            if (file != null && !file.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(file);
                storageRequest.setImUrl(imageUrl);
            }

            // Mặc định tạo zones tự động
            storageRequest.setCreateZones(true);

            // Gọi service lưu vào DB
            Storage createdStorage = storageService.createStorage(storageRequest);

            // Tính số zones đã tạo
            int zonesCreated = (int) Math.floor(createdStorage.getArea() / 50.0);
            String message = "Thêm kho thành công! Đã tạo " + zonesCreated + " khu cho thuê (mỗi khu 50m²).";

            redirectAttributes.addFlashAttribute("message", message);
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Lỗi khi thêm kho: " + e.getMessage());
        }
        return "redirect:/admin/manager-dashboard";
    }

    @GetMapping("/manager-dashboard/storages/{id}")
    public String viewStorageDetail(@PathVariable int id, Model model, HttpSession session) {
        // Populate user info for manager taskbar
        Manager loggedInManager = (Manager) session.getAttribute("loggedInManager");
        if (loggedInManager != null) {
            model.addAttribute("user", loggedInManager.getFullname());
            model.addAttribute("userName", loggedInManager.getEmail());
            model.addAttribute("userRole", "MANAGER");
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && "anonymousUser".equals(auth.getPrincipal()) == false) {
                UserDetails userDetails = (UserDetails) auth.getPrincipal();
                model.addAttribute("userName", userDetails.getUsername());
                model.addAttribute("userRole", auth.getAuthorities().iterator().next().getAuthority());
            }
        }

        Optional<Storage> optionalStorage = storageService.findByID(id);
        if (optionalStorage.isPresent()) {
            Storage storage = optionalStorage.get();
            model.addAttribute("storage", storage);

            double totalArea = storage.getArea();
            double rentedArea = orderService.getTotalRentedArea(id);
            double remainingArea = Math.max(0.0, totalArea - rentedArea);
            String staffName = (storage.getStaff() != null && storage.getStaff().getFullname() != null)
                    ? storage.getStaff().getFullname()
                    : "Chưa có nhân viên quản lý";

            // Lấy danh sách zones của storage này
            List<Zone> zones = zoneService.getZonesByStorageId(id);
            double totalZoneArea = zoneService.getTotalZoneAreaByStorageId(id);
            double availableAreaForNewZone = totalArea - totalZoneArea;

            model.addAttribute("totalArea", totalArea);
            model.addAttribute("rentedArea", rentedArea);
            model.addAttribute("remainingArea", remainingArea);
            model.addAttribute("staffName", staffName);
            model.addAttribute("zones", zones);
            model.addAttribute("totalZoneArea", totalZoneArea);
            model.addAttribute("availableAreaForNewZone", availableAreaForNewZone);
        } else {
            return "redirect:/admin/manager-dashboard";
        }
        return "manager-storage-detail";
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
            @ModelAttribute StorageRequest storageRequest,
            @RequestParam(value = "returnUrl", required = false) String returnUrl) {
        Optional<Storage> optional = storageService.findByID(id);
        if (optional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy kho!");
            return "redirect:/admin/manager-dashboard";
        }

        storageService.updateStorage(storageRequest, optional.get());
        redirectAttributes.addFlashAttribute("message", "Cập nhật thành công!");

        if (returnUrl != null && !returnUrl.isBlank()) {
            return "redirect:" + returnUrl;
        }
        return "redirect:/admin/manager-dashboard/storages/";
    }

    @PostMapping("/manager-dashboard/storages/{id}")
    public String updateStoragePost(@PathVariable int id,
            RedirectAttributes redirectAttributes,
            @ModelAttribute StorageRequest storageRequest,
            @RequestParam(value = "returnUrl", required = false) String returnUrl,
            @RequestParam(value = "newImage", required = false) MultipartFile newImage,
            Model model) {
        Optional<Storage> optional = storageService.findByID(id);
        if (optional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy kho!");
            return "redirect:/admin/manager-dashboard";
        }

        Storage storage = optional.get();

        // Xử lý upload ảnh mới nếu có
        if (newImage != null && !newImage.isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadImage(newImage);
                storage.setImUrl(imageUrl);
                redirectAttributes.addFlashAttribute("message", "Cập nhật kho và ảnh thành công!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Lỗi khi upload ảnh: " + e.getMessage());
                return "redirect:/admin/manager-dashboard/storages/" + id + "/edit";
            }
        } else {
            redirectAttributes.addFlashAttribute("message", "Cập nhật kho thành công!");
        }

        // Cập nhật thông tin kho
        Storage updated = storageService.updateStorage(storageRequest, storage);
        model.addAttribute("storage", updated);

        if (returnUrl != null && !returnUrl.isBlank()) {
            return "redirect:" + returnUrl;
        }
        return "redirect:/admin/manager-dashboard/storages/" + id;
    }

    //danh sách staff
    @GetMapping("/staff-list")
    public String showStaffList(Model model,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "6") int size) {

        Page<Staff> staffPage = staffService.getStaffsByPage(page - 1, size); // already DESC by id inside

        model.addAttribute("staffPage", staffPage);
        model.addAttribute("staffs", staffPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", staffPage.getTotalPages());
        model.addAttribute("totalStaff", (int) staffPage.getTotalElements());
        model.addAttribute("size", size);
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



    @PostMapping("/staffs/{id}/toggle-block")
    public String toggleStaffBlock(
            @PathVariable int id,
            @RequestParam(value = "from", required = false) String from,
            RedirectAttributes ra) {

        Staff s = staffService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Staff not found"));

        if (s.getRoleName() == RoleName.BLOCKED) {
            s.setRoleName(RoleName.STAFF);
            ra.addFlashAttribute("success", "Đã mở khóa nhân viên #" + id);
        } else {
            s.setRoleName(RoleName.BLOCKED);
            ra.addFlashAttribute("success", "Đã khóa nhân viên #" + id);
        }
        staffService.save(s);

        return "detail".equals(from)
                ? ("redirect:/admin/staff-list/detail/" + id)
                : "redirect:/admin/staff-list";
    }
    // ==== ADD STAFF (form) ====
    @GetMapping("/staff-list/add")
    public String showAddStaffForm(Model model) {
        model.addAttribute("form", new com.example.swp.dto.NewStaffForm());
        return "staff-add"; // new view below
    }

    @PostMapping("/staff-list/add")
    public String handleAddStaff(
            @ModelAttribute("form") @jakarta.validation.Valid com.example.swp.dto.NewStaffForm form,
            org.springframework.validation.BindingResult binding,
            RedirectAttributes ra,
            Model model
    ) {
        if (binding.hasErrors()) {
            return "staff-add";
        }
        try {
            staffService.createFromForm(form);
            ra.addFlashAttribute("success", "Đã tạo nhân viên và gửi mật khẩu qua email.");
            return "redirect:/admin/staff-list";
        } catch (IllegalArgumentException ex) {
            switch (ex.getMessage()) {
                case "duplicate-email":
                    binding.rejectValue("email", "duplicate", "Email đã tồn tại");
                    break;
                case "duplicate-phone":
                    binding.rejectValue("phone", "duplicate", "SĐT đã tồn tại");
                    break;
                default:
                    binding.reject("db", "Email hoặc SĐT đã tồn tại");
            }
            return "staff-add";
        }
    }

    // ==== DELETE STAFF ====
    @PostMapping("/staffs/{id}/delete")
    public String deleteStaff(
            @PathVariable int id,
            @RequestParam(value = "from", required = false) String from,
            RedirectAttributes ra
    ) {
        try {
            staffService.deleteById(id);
            ra.addFlashAttribute("success", "Đã xoá nhân viên #" + id);
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        // Usually go back to list
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

    @GetMapping("/storages/{id}/add-zone")
    public String showAddZoneForm(@PathVariable int id, Model model, HttpSession session) {
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

        Optional<Storage> optionalStorage = storageService.findByID(id);
        if (optionalStorage.isPresent()) {
            Storage storage = optionalStorage.get();
            model.addAttribute("storage", storage);

            // Tính toán diện tích còn lại có thể tạo zone
            double totalArea = storage.getArea();
            double totalZoneArea = zoneService.getTotalZoneAreaByStorageId(id);
            double availableArea = totalArea - totalZoneArea;

            model.addAttribute("availableArea", availableArea);
            model.addAttribute("zoneRequest", new ZoneRequest());
        } else {
            return "redirect:/admin/manager-dashboard";
        }

        return "add-zone";
    }

    @LogActivity(action = "Thêm khu vực chứa đồ")
    @PostMapping("/storages/{id}/add-zone")
    public String addZone(@PathVariable int id,
            @Valid @ModelAttribute ZoneRequest zoneRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        // Kiểm tra validation errors
        if (bindingResult.hasErrors()) {
            Optional<Storage> optionalStorage = storageService.findByID(id);
            if (optionalStorage.isPresent()) {
                Storage storage = optionalStorage.get();
                model.addAttribute("storage", storage);
                model.addAttribute("zoneRequest", zoneRequest);
                double totalZoneArea = zoneService.getTotalZoneAreaByStorageId(id);
                model.addAttribute("availableArea", storage.getArea() - totalZoneArea);
                return "add-zone";
            }
            return "redirect:/admin/manager-dashboard";
        }

        try {
            Optional<Storage> optionalStorage = storageService.findByID(id);
            if (optionalStorage.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy kho!");
                return "redirect:/admin/manager-dashboard";
            }

            Storage storage = optionalStorage.get();

            // Validate diện tích zone
            if (!zoneService.validateZoneArea(id, zoneRequest.getZoneArea())) {
                redirectAttributes.addFlashAttribute("error", "Diện tích zone vượt quá diện tích còn lại của kho!");
                return "redirect:/admin/storages/" + id + "/add-zone";
            }

            // Tạo zone mới
            Zone newZone = new Zone();
            newZone.setName(zoneRequest.getName());
            newZone.setZoneArea(zoneRequest.getZoneArea());
            newZone.setPricePerDay(zoneRequest.getPricePerDay());
            newZone.setStatus(zoneRequest.getStatus() != null ? zoneRequest.getStatus().getValue() : ZoneStatus.AVAILABLE.getValue());
            newZone.setStorage(storage);

            zoneService.createZone(newZone);
            redirectAttributes.addFlashAttribute("message", "Thêm khu vực thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm khu vực: " + e.getMessage());
        }

        return "redirect:/admin/manager-dashboard/storages/" + id;
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

    @GetMapping("/storages/{storageId}/zones/{zoneId}/edit")
    public String showEditZoneForm(@PathVariable int storageId, @PathVariable int zoneId,
            Model model, HttpSession session) {
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

        Optional<Storage> optionalStorage = storageService.findByID(storageId);
        Optional<Zone> optionalZone = zoneService.getZoneById(zoneId);

        if (optionalStorage.isEmpty() || optionalZone.isEmpty()) {
            return "redirect:/admin/manager-dashboard/storages/" + storageId;
        }

        Storage storage = optionalStorage.get();
        Zone zone = optionalZone.get();

        // Kiểm tra xem zone có thuộc về storage này không
        if (zone.getStorage().getStorageid() != storageId) {
            return "redirect:/admin/manager-dashboard/storages/" + storageId;
        }

        // Tạo ZoneRequest từ Zone hiện tại
        ZoneRequest zoneRequest = new ZoneRequest();
        zoneRequest.setName(zone.getName());
        zoneRequest.setZoneArea(zone.getZoneArea());
        zoneRequest.setPricePerDay(zone.getPricePerDay());
        zoneRequest.setStatus(ZoneStatus.fromValue(zone.getStatus()));
        zoneRequest.setStorageId(storageId);

        // Tính diện tích có thể sử dụng (bao gồm cả zone hiện tại)
        double totalZoneArea = zoneService.getTotalZoneAreaByStorageId(storageId);
        double availableArea = storage.getArea() - totalZoneArea + zone.getZoneArea();

        model.addAttribute("storage", storage);
        model.addAttribute("zone", zone);
        model.addAttribute("zoneRequest", zoneRequest);
        model.addAttribute("availableArea", availableArea);
        model.addAttribute("isEdit", true);

        return "edit-zone";
    }

    @PostMapping("/storages/{storageId}/zones/{zoneId}/edit")
    public String updateZone(@PathVariable int storageId, @PathVariable int zoneId,
            @Valid @ModelAttribute ZoneRequest zoneRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        // Kiểm tra validation errors
        if (bindingResult.hasErrors()) {
            Optional<Storage> optionalStorage = storageService.findByID(storageId);
            Optional<Zone> optionalZone = zoneService.getZoneById(zoneId);

            if (optionalStorage.isPresent() && optionalZone.isPresent()) {
                Storage storage = optionalStorage.get();
                Zone zone = optionalZone.get();

                model.addAttribute("storage", storage);
                model.addAttribute("zone", zone);
                model.addAttribute("zoneRequest", zoneRequest);
                model.addAttribute("isEdit", true);

                // Tính diện tích có thể sử dụng
                double totalZoneArea = zoneService.getTotalZoneAreaByStorageId(storageId);
                double availableArea = storage.getArea() - totalZoneArea + zone.getZoneArea();
                model.addAttribute("availableArea", availableArea);

                return "edit-zone";
            }
            return "redirect:/admin/manager-dashboard/storages/" + storageId;
        }

        try {
            Optional<Zone> optionalZone = zoneService.getZoneById(zoneId);
            if (optionalZone.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy khu vực!");
                return "redirect:/admin/manager-dashboard/storages/" + storageId;
            }

            Zone existingZone = optionalZone.get();

            // Kiểm tra xem zone có thuộc về storage này không
            if (existingZone.getStorage().getStorageid() != storageId) {
                redirectAttributes.addFlashAttribute("error", "Khu vực không thuộc về kho này!");
                return "redirect:/admin/manager-dashboard/storages/" + storageId;
            }

            // Validate diện tích zone (trừ đi diện tích zone hiện tại)
            double currentZoneArea = existingZone.getZoneArea();
            double newTotalArea = zoneService.getTotalZoneAreaByStorageId(storageId) - currentZoneArea + zoneRequest.getZoneArea();

            Optional<Storage> optionalStorage = storageService.findByID(storageId);
            if (optionalStorage.isPresent() && newTotalArea > optionalStorage.get().getArea()) {
                redirectAttributes.addFlashAttribute("error", "Tổng diện tích các khu vực vượt quá diện tích kho!");
                return "redirect:/admin/storages/" + storageId + "/zones/" + zoneId + "/edit";
            }

            // Cập nhật thông tin zone
            existingZone.setName(zoneRequest.getName());
            existingZone.setZoneArea(zoneRequest.getZoneArea());
            existingZone.setPricePerDay(zoneRequest.getPricePerDay());
            existingZone.setStatus(zoneRequest.getStatus() != null ? zoneRequest.getStatus().getValue() : ZoneStatus.AVAILABLE.getValue());

            zoneService.updateZone(existingZone);
            redirectAttributes.addFlashAttribute("message", "Cập nhật khu vực thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật khu vực: " + e.getMessage());
        }

        return "redirect:/admin/manager-dashboard/storages/" + storageId;
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
        List<EContract> allContracts = eContractService.findAll();

        // Chỉ hiển thị hợp đồng từ các đơn hàng đã thanh toán (PAID) hoặc đã được duyệt (APPROVED)
        List<EContract> contracts = allContracts.stream()
                .filter(contract -> {
                    String orderStatus = contract.getOrder().getStatus();
                    return "PAID".equals(orderStatus) || "APPROVED".equals(orderStatus);
                })
                .collect(java.util.stream.Collectors.toList());

        // Thống kê hợp đồng dựa trên danh sách đã lọc
        long signedContracts = contracts.stream()
                .filter(contract -> contract.getStatus().name().equals("SIGNED"))
                .count();
        long pendingContracts = contracts.stream()
                .filter(contract -> contract.getStatus().name().equals("PENDING"))
                .count();
        long cancelledContracts = contracts.stream()
                .filter(contract -> contract.getStatus().name().equals("CANCELLED"))
                .count();
        long pendingCancellationContracts = contracts.stream()
                .filter(contract -> contract.getStatus().name().equals("PENDING_CANCELLATION"))
                .count();

        System.out.println("DEBUG ADMIN: Total contracts found: " + allContracts.size());
        System.out.println("DEBUG ADMIN: Filtered contracts (PAID/APPROVED): " + contracts.size());
        System.out.println("DEBUG ADMIN: Signed contracts: " + signedContracts);
        System.out.println("DEBUG ADMIN: Pending contracts: " + pendingContracts);
        System.out.println("DEBUG ADMIN: Pending cancellation contracts: " + pendingCancellationContracts);

        model.addAttribute("contracts", contracts);
        model.addAttribute("totalContracts", contracts.size());
        model.addAttribute("signedContracts", signedContracts);
        model.addAttribute("pendingContracts", pendingContracts);
        model.addAttribute("cancelledContracts", cancelledContracts);
        model.addAttribute("pendingCancellationContracts", pendingCancellationContracts);

        return "admin-contracts";
    }

    /**
     * Xóa khu vực
     */
    @PostMapping("/storages/{storageId}/zones/{zoneId}/delete")
    public String deleteZone(@PathVariable int storageId,
            @PathVariable int zoneId,
            RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra xem zone có tồn tại không
            Optional<Zone> optionalZone = zoneService.getZoneById(zoneId);
            if (optionalZone.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Khu vực không tồn tại!");
                return "redirect:/admin/manager-dashboard/storages/" + storageId;
            }

            Zone zone = optionalZone.get();

            // Kiểm tra xem zone có thuộc về storage này không
            if (zone.getStorage().getStorageid() != storageId) {
                redirectAttributes.addFlashAttribute("error", "Khu vực không thuộc về kho này!");
                return "redirect:/admin/manager-dashboard/storages/" + storageId;
            }

            // Kiểm tra xem zone có đang được sử dụng trong đơn hàng nào không
            List<Order> ordersUsingZone = orderRepository.findAll().stream()
                    .filter(order -> order.getZone() != null && order.getZone().getId() == zoneId)
                    .collect(java.util.stream.Collectors.toList());
            if (!ordersUsingZone.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không thể xóa khu vực đang được sử dụng trong đơn hàng!");
                return "redirect:/admin/manager-dashboard/storages/" + storageId;
            }

            // Kiểm tra xem zone có đang được sử dụng trong đơn hàng có trạng thái đặc biệt không
            List<Order> activeOrdersUsingZone = orderRepository.findAll().stream()
                    .filter(order -> order.getZone() != null && order.getZone().getId() == zoneId)
                    .filter(order -> "PENDING".equals(order.getStatus()) || "APPROVED".equals(order.getStatus()) || "PAID".equals(order.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
            if (!activeOrdersUsingZone.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không thể xóa khu vực đang được sử dụng trong đơn hàng đang hoạt động!");
                return "redirect:/admin/manager-dashboard/storages/" + storageId;
            }

            // Kiểm tra xem zone có đang được sử dụng trong đơn hàng có ngày thuê hiện tại hoặc trong tương lai không
            java.time.LocalDate today = java.time.LocalDate.now();
            List<Order> currentOrdersUsingZone = orderRepository.findAll().stream()
                    .filter(order -> order.getZone() != null && order.getZone().getId() == zoneId)
                    .filter(order -> order.getStartDate() != null && order.getEndDate() != null)
                    .filter(order -> !today.isAfter(order.getEndDate()))
                    .collect(java.util.stream.Collectors.toList());
            if (!currentOrdersUsingZone.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không thể xóa khu vực đang được sử dụng trong đơn hàng hiện tại hoặc trong tương lai!");
                return "redirect:/admin/manager-dashboard/storages/" + storageId;
            }

            // Kiểm tra xem zone có đang được sử dụng không
            if (!"available".equals(zone.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "Chỉ có thể xóa khu vực có trạng thái 'Còn trống'!");
                return "redirect:/admin/manager-dashboard/storages/" + storageId;
            }

            // Lưu thông tin về diện tích zone trước khi xóa
            double deletedZoneArea = zone.getZoneArea();

            // Kiểm tra xem có phải là zone cuối cùng không
            List<Zone> remainingZones = zoneService.getZonesByStorageId(storageId);
            boolean isLastZone = remainingZones.size() == 1;

            // Xóa zone
            zoneService.deleteZone(zoneId);

            if (isLastZone) {
                redirectAttributes.addFlashAttribute("message", "Xóa khu vực cuối cùng thành công! Kho này hiện không có khu vực nào.");
            } else {
                redirectAttributes.addFlashAttribute("message", "Xóa khu vực thành công! Đã giải phóng " + deletedZoneArea + " m².");
            }
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xóa khu vực: " + e.getMessage());
        }

        return "redirect:/admin/manager-dashboard/storages/" + storageId;
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
