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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.swp.entity.Customer;
import com.example.swp.entity.Manager;
import com.example.swp.entity.Staff;
import com.example.swp.service.CustomerService;
import com.example.swp.service.StaffService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class ManagerController {



    @Autowired
    CustomerService customerService;

    @Autowired
    private StaffService staffService;



    @GetMapping("/manager-dashboard")
    public String showDashboard(Model model, HttpSession session) {
        Manager loggedInManager = (Manager) session.getAttribute("loggedInManager");
        if (loggedInManager != null) {
            model.addAttribute("user", loggedInManager.getFullname());
            model.addAttribute("userName", loggedInManager.getEmail());
            model.addAttribute("userRole", "Manager");
        }

        List<Customer> customers = customerService.getAll();
        int totalUser = customers.size();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            model.addAttribute("userName", userDetails.getUsername());
            model.addAttribute("userRole", auth.getAuthorities().iterator().next().getAuthority());
        }

        return "admin";
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




    @GetMapping("/manager-customer-list")
    public String showUserList(Model model) {
        List<Customer> customers = customerService.getAll();
        int totalCustomers = customers.size();
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("customers", customers);
        return "manager-customer-list";
    }

    // @GetMapping("/manager/profile")
    // public String managerProfilePage(HttpSession session, Model model) {
    // Manager loggedInManager = (Manager) session.getAttribute("loggedInManager");
    // if (loggedInManager != null) {
    // model.addAttribute("user", loggedInManager); // -> biến 'user' trong HTML
    // }
    // return "manager-setting";
    // }



}
