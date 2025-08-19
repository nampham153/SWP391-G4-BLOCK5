package com.example.swp.controller.website;

import com.example.swp.entity.Customer;
import com.example.swp.entity.Staff;
import com.example.swp.entity.Manager;
import com.example.swp.enums.RoleName;
import com.example.swp.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/SWP")
public class CustomerListController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/customers")
    public String listCustomers(
            @RequestParam(value = "role", required = false, defaultValue = "ALL") String role,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "customerId", required = false) Integer customerId,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        // Kiểm tra quyền truy cập - chỉ staff mới được vào
        Staff staff = (Staff) session.getAttribute("loggedInStaff");
        Manager manager = (Manager) session.getAttribute("loggedInManager");
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        
        if (customer != null) {
            redirectAttributes.addFlashAttribute("error", 
                "Bạn không có quyền truy cập trang này. Trang này chỉ dành cho nhân viên.");
            return "redirect:/SWP";
        }
        
        if (manager != null) {
            redirectAttributes.addFlashAttribute("error", 
                "Bạn không có quyền truy cập trang này. Trang này chỉ dành cho nhân viên.");
            return "redirect:/admin/manager-customer-list";
        }
        
        if (staff == null) {
            redirectAttributes.addFlashAttribute("error", 
                "Vui lòng đăng nhập với tài khoản nhân viên để truy cập trang này.");
            return "redirect:/api/login";
        }
        List<Customer> customers;
        if (customerId != null) {
            Customer found = customerService.getCustomer(customerId);
            customers = found != null ? List.of(found) : List.of();
            model.addAttribute("selectedRole", "ALL");
        } else if (name != null && !name.isEmpty()) {
            customers = customerService.searchByName(name);
            model.addAttribute("selectedRole", "ALL");
        } else if (!"ALL".equalsIgnoreCase(role)) {
            customers = customerService.filterByRole(RoleName.valueOf(role));
            model.addAttribute("selectedRole", role);
        } else {
            customers = customerService.getAll();
            model.addAttribute("selectedRole", "ALL");
        }
        model.addAttribute("customers", customers);
        return "customer-list";
    }

    @GetMapping("/customers/{id}")
    public String customerDetail(@PathVariable int id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        
        // Kiểm tra quyền truy cập - chỉ staff mới được vào
        Staff staff = (Staff) session.getAttribute("loggedInStaff");
        Manager manager = (Manager) session.getAttribute("loggedInManager");
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        
        if (customer != null) {
            redirectAttributes.addFlashAttribute("error", 
                "Bạn không có quyền truy cập trang này. Trang này chỉ dành cho nhân viên.");
            return "redirect:/SWP";
        }
        
        if (manager != null) {
            redirectAttributes.addFlashAttribute("error", 
                "Bạn không có quyền truy cập trang này. Trang này chỉ dành cho nhân viên.");
            return "redirect:/admin/manager-customer-list";
        }
        
        if (staff == null) {
            redirectAttributes.addFlashAttribute("error", 
                "Vui lòng đăng nhập với tài khoản nhân viên để truy cập trang này.");
            return "redirect:/api/login";
        }
        Customer foundCustomer = customerService.getCustomer(id);
        if (foundCustomer == null) {
            return "redirect:/SWP/customers";
        }
        model.addAttribute("customer", foundCustomer);
        return "customer-detail";
    }

    @PostMapping("/customers/delete/{id}")
    public String toggleCustomerAccountStatus(@PathVariable int id, RedirectAttributes redirectAttributes, HttpSession session) {
        
        // Kiểm tra quyền truy cập - chỉ staff mới được thực hiện
        Staff staff = (Staff) session.getAttribute("loggedInStaff");
        Manager manager = (Manager) session.getAttribute("loggedInManager");
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        
        if (customer != null) {
            redirectAttributes.addFlashAttribute("error", 
                "Bạn không có quyền thực hiện hành động này.");
            return "redirect:/SWP";
        }
        
        if (manager != null) {
            redirectAttributes.addFlashAttribute("error", 
                "Bạn không có quyền thực hiện hành động này.");
            return "redirect:/admin";
        }
        
        if (staff == null) {
            redirectAttributes.addFlashAttribute("error", 
                "Vui lòng đăng nhập với tài khoản nhân viên để thực hiện hành động này.");
            return "redirect:/api/login";
        }
        try {
            Customer targetCustomer = customerService.getCustomer(id);
            if (targetCustomer == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy khách hàng!");
                return "redirect:/admin/manager-customer-list";
            }

            String action = targetCustomer.getRoleName() == RoleName.BLOCKED ? "mở khóa" : "khóa";
            customerService.toggleAccountStatus(id);
            redirectAttributes.addFlashAttribute("success",
                    "Đã " + action + " tài khoản khách hàng " + targetCustomer.getFullname() + " thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/manager-customer-list";
    }
}
