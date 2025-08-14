package com.example.swp.controller.website;

import com.cloudinary.Cloudinary;
import com.example.swp.annotation.LogActivity;
import com.example.swp.entity.*;
import com.example.swp.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class ManagerController {

    @Autowired
    CustomerService customerService;

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
