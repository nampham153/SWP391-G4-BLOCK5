package com.example.swp.controller.website;

import com.example.swp.annotation.LogActivity;
import com.example.swp.entity.Customer;
import com.example.swp.enums.RoleName;
import com.example.swp.service.CustomerService;
import com.example.swp.service.EmailService;
import com.example.swp.service.OtpCache;
import com.example.swp.service.StaffService;
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
    private StaffService staffService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private OtpCache otpCache;
    @Autowired
    private EmailService emailService;
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
        // Normalize (optional but safer if DB is case-sensitive on email)
        String email = customer.getEmail() == null ? null : customer.getEmail().trim();
        String phone = customer.getPhone() == null ? null : customer.getPhone().trim();

        // Email duplicate across BOTH tables
        boolean emailTaken =
                customerService.existsByEmail(email) ||    // Customer table
                        staffService.emailExists(email);            // Staff table

        if (emailTaken) {
            return ResponseEntity.badRequest().body("Email đã tồn tại (khách hàng hoặc nhân viên)!");
        }

        // Phone duplicate across BOTH tables
        boolean phoneTaken =
                customerService.existsByPhone(phone) ||    // Customer table
                        staffService.phoneExists(phone);           // Staff table

        if (phoneTaken) {
            return ResponseEntity.badRequest().body("Số điện thoại đã tồn tại (khách hàng hoặc nhân viên)!");
        }

        customer.setRoleName(RoleName.BLOCKED);
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customerService.save(customer);

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        emailService.sendOtpEmail(email, otp);
        otpCache.put(email, otp);

        return ResponseEntity.ok("Đăng ký thành công!");
    }

    @PostMapping("/api/verify-otp")
    @ResponseBody
    public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        String cachedOtp = otpCache.get(email);
        if (cachedOtp == null) {
            return ResponseEntity.badRequest().body("OTP đã hết hạn hoặc không tồn tại!");
        }
        if (!cachedOtp.equals(otp)) {
            return ResponseEntity.badRequest().body("OTP không đúng!");
        }

        Customer customer = customerService.findByEmail(email);
        if (customer == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy tài khoản!");
        }

        customer.setRoleName(RoleName.CUSTOMER);
        customerService.save(customer);
        otpCache.remove(email);

        return ResponseEntity.ok("Xác thực thành công! Bạn có thể đăng nhập.");
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
