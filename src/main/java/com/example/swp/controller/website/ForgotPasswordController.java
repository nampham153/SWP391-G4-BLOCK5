package com.example.swp.controller.website;

import com.example.swp.dto.ForgotPasswordRequest;
import com.example.swp.entity.Customer;
import com.example.swp.service.CustomerService;
import com.example.swp.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@Controller
public class ForgotPasswordController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    // GET: Hiển thị form quên mật khẩu
    @GetMapping("/customer-forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
        return "customer-forgot-password";
    }

    // POST: Xử lý gửi email
    @PostMapping("/customer-forgot-password")
    public String handleForgotPassword(
            @ModelAttribute("forgotPasswordRequest") @Valid ForgotPasswordRequest form,
            BindingResult bindingResult,
            Model model
    ) {
        // Nếu có lỗi validate → trả lại form
        if (bindingResult.hasErrors()) {
            return "customer-forgot-password";
        }

        Customer customer = customerService.findByEmail(form.getEmail());
        if (customer == null) {
            bindingResult.rejectValue("email", "notfound", "Email không tồn tại!");
            return "customer-forgot-password";
        }

        // Sinh mật khẩu mới
        String newPassword = UUID.randomUUID().toString().substring(0, 8);
        customer.setPassword(passwordEncoder.encode(newPassword));
        customerService.save(customer);

        // Gửi email
        String subject = "Khôi phục mật khẩu";
        String body = "Xin chào " + customer.getFullname() + ",\n\n"
                + "Mật khẩu mới của bạn là: " + newPassword + "\n\n"
                + "Vui lòng đăng nhập và đổi lại mật khẩu ngay.";
        emailService.sendEmail(customer.getEmail(), subject, body);

        // Reset form và hiển thị thông báo thành công
        model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
        model.addAttribute("message", "Mật khẩu mới đã được gửi đến email của bạn!");

        return "customer-forgot-password";
    }
}
