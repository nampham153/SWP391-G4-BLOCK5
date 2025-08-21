package com.example.swp.controller.website;

import com.example.swp.entity.Customer;
import com.example.swp.service.CustomerService;
import com.example.swp.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final CustomerService customerService;
    private final NotificationService notificationService;

    @Autowired
    public HomeController(CustomerService customerService,
                          NotificationService notificationService) {
        this.customerService = customerService;
        this.notificationService = notificationService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/SWP";
    }

    @GetMapping("/home-page")
    public String homeRedirect() {
        return "redirect:/SWP";
    }

    @GetMapping("/SWP")
    public String swpHome(Model model, HttpSession session) {
        String email = (String) session.getAttribute("email");
        boolean isLoggedIn = (email != null);
        model.addAttribute("isLoggedIn", isLoggedIn);

        if (isLoggedIn) {
            Customer customer = customerService.findByEmail(email);
            model.addAttribute("customer", customer);

            long unreadCount = 0L;
            if (customer != null) {
                try {
                    unreadCount = notificationService.countUnreadNotifications(customer);
                } catch (Exception ignored) { /* keep 0 if anything goes wrong */ }
            }
            model.addAttribute("unreadCount", unreadCount);
        }

        return "home-page"; // your home-page.html
    }
}
