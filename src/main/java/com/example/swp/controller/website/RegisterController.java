package com.example.swp.controller.website;

import com.example.swp.annotation.LogActivity;
import com.example.swp.entity.Customer;
import com.example.swp.enums.RoleName;
import com.example.swp.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@Controller
public class RegisterController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "register";
    }

    @LogActivity(action = "Người dùng đăng ký tài khoản mới")
    @PostMapping("/api/register")
    @ResponseBody
    public ResponseEntity<String> processRegister(@Valid @RequestBody Customer customer) {
        if (customerService.existsByEmail(customer.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã tồn tại!");
        }
        if (customerService.existsByPhone(customer.getPhone())) {
            return ResponseEntity.badRequest().body("Số điện thoại đã tồn tại!");
        }

        customer.setRoleName(RoleName.CUSTOMER);
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customerService.save(customer);

        return ResponseEntity.ok("Đăng ký thành công!");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errorMessages = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(err -> err.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest().body(errorMessages);
    }

}
