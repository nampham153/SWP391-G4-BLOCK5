package com.example.swp.controller.website;

import com.example.swp.entity.Customer;
import com.example.swp.enums.RoleName;
import com.example.swp.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
            Model model) {
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
}
