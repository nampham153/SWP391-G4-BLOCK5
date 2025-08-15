package com.example.swp.controller.website;

import com.example.swp.entity.Customer;
import com.example.swp.enums.RoleName;
import com.example.swp.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegisterController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/api/auth/register")
    @ResponseBody
    public String processRegister(@RequestBody Customer customer) {
        try {
            // Check if email already exists
            if (customerService.existsByEmail(customer.getEmail())) {
                return "Email đã tồn tại trong hệ thống!";
            }
            
            // Check if phone already exists
            if (customerService.existsByPhone(customer.getPhone())) {
                return "Số điện thoại đã tồn tại trong hệ thống!";
            }

            // Set default values
            customer.setRoleName(RoleName.CUSTOMER);
            customer.setPoints(5);
            customer.setIsOnline(false);
            
            // Save customer
            Customer savedCustomer = customerService.save(customer);
            if (savedCustomer != null && savedCustomer.getId() > 0) {
                return "Đăng ký thành công!";
            } else {
                return "Đăng ký thất bại: Không thể lưu thông tin khách hàng";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Đăng ký thất bại: " + e.getMessage();
        }
    }
}
