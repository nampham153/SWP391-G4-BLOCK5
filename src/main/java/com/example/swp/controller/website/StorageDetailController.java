package com.example.swp.controller.website;

import com.example.swp.entity.Customer;
import com.example.swp.entity.Manager;
import com.example.swp.entity.Staff;
import com.example.swp.entity.Storage;
import com.example.swp.service.StorageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/SWP")
public class StorageDetailController {

    @Autowired
    private StorageService storageService;

    @GetMapping("/storages/{id}")
    public String viewStorageDetail(@PathVariable("id") int storageId,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Kiểm tra quyền truy cập - chỉ customer mới được vào
        Staff staff = (Staff) session.getAttribute("loggedInStaff");
        Manager manager = (Manager) session.getAttribute("loggedInManager");
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");

        if (staff != null) {
            redirectAttributes.addFlashAttribute("error",
                    "Trang này chỉ dành cho khách hàng. Bạn đã được chuyển về trang nhân viên.");
            return "redirect:/SWP/customers";
        }

        if (manager != null) {
            redirectAttributes.addFlashAttribute("error",
                "Trang này chỉ dành cho khách hàng. Bạn đã được chuyển về trang quản lý.");
            return "redirect:/admin/manager-customer-list";
        }
        
        if (customer == null) {
            redirectAttributes.addFlashAttribute("error",
                    "Vui lòng đăng nhập với tài khoản khách hàng để truy cập trang này.");
            return "redirect:/api/login";
        }

        Optional<Storage> optionalStorage = storageService.findByID(storageId);
        if (optionalStorage.isEmpty()) {
            return "redirect:/SWP/storages";
        }

        Storage storage = optionalStorage.get();
        model.addAttribute("storage", storage);

        // Thêm thông tin customer vào model
        model.addAttribute("customer", customer);

        return "storage-detail";
    }

}