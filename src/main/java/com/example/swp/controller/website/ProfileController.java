package com.example.swp.controller.website;

import com.example.swp.dto.ChangePasswordRequest;
import com.example.swp.dto.ProfileUpdateRequest;
import com.example.swp.dto.ForgotPasswordRequest;
import com.example.swp.entity.Customer;
import com.example.swp.service.ActivityLogService;
import com.example.swp.service.CustomerService;
import com.example.swp.service.EmailService;
import com.example.swp.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Controller
public class ProfileController {

    @Autowired
    private CustomerService customerService;
    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ActivityLogService activityLogService;



    /**
     * Trang profile khách hàng
     */
    @GetMapping("/profile")
    public String profile(Model model, HttpSession session,
                          @RequestParam(value = "tab", required = false, defaultValue = "profile") String tab) {
        String email = (String) session.getAttribute("email");

        // Nếu chưa đăng nhập, chuyển về trang đăng nhập, hoặc bạn có thể set message lỗi
        if (email == null) {
            model.addAttribute("error", "Bạn chưa đăng nhập.");
            return "redirect:/login";
        }

        Customer customer = customerService.findByEmail(email);

        ProfileUpdateRequest customerProfile = new ProfileUpdateRequest();
        ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest();
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();

        if (customer != null) {
            customerProfile.setFullname(customer.getFullname());
            customerProfile.setPhone(customer.getPhone());
            customerProfile.setEmail(customer.getEmail());
            customerProfile.setAddress(customer.getAddress());
        }

        model.addAttribute("customerProfile", customerProfile);
        model.addAttribute("forgotPasswordRequest", forgotPasswordRequest);
        model.addAttribute("changePasswordRequest", changePasswordRequest);
        model.addAttribute("customer", customer); // customer có thể null, nhớ kiểm tra trong template!
        model.addAttribute("activeTab", tab);
        return "customer-profile";
    }

    /**
     * Cập nhật thông tin profile + upload avatar
     */
    @PostMapping("/update-profile")
    public String updateProfile(
            @ModelAttribute("customerProfile") @Valid ProfileUpdateRequest form,
            BindingResult bindingResult,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            Model model,
            HttpSession session
    ) {
        String email = (String) session.getAttribute("email");
        if (email == null) {
            model.addAttribute("error", "Bạn chưa đăng nhập.");
            return "redirect:/login";
        }

        Customer customer = customerService.findByEmail(email);

        model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
        model.addAttribute("customer", customer);
        model.addAttribute("activeTab", "update");

        if (bindingResult.hasErrors()) return "customer-profile";
        if (customer != null) {
            customer.setFullname(form.getFullname());
            customer.setPhone(form.getPhone());
            customer.setAddress(form.getAddress());

            if (avatarFile != null && !avatarFile.isEmpty()) {
                try {
                    customer.setAvatar(avatarFile.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            customerService.save(customer);
            notificationService.createNotification("Bạn vừa cập nhật thông tin cá nhân thành công.", customer);

            activityLogService.logActivity(
                    "Cập nhật hồ sơ",
                    "Khách hàng " + customer.getFullname() + " đã cập nhật thông tin cá nhân.",
                    customer,
                    null, null, null, null, null
            );

            Customer updated = customerService.findByEmail(email);
            model.addAttribute("customerProfile", form);
            model.addAttribute("customer", updated);
            model.addAttribute("success", "Cập nhật thành công!");
            return "customer-profile";
        }
        model.addAttribute("error", "Không xác định được khách hàng.");
        return "customer-profile";
    }

    /**
     * Trả về ảnh đại diện từ database (dùng cho <img th:src="@{/profile/avatar/{id}(id=${customer.id})}"/>)
     */
    @GetMapping("/profile/avatar/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getAvatar(@PathVariable int id) {
        Customer customer = customerService.getCustomer(id);
        if (customer != null && customer.getAvatar() != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); // Hoặc IMAGE_PNG tùy bạn lưu
            return new ResponseEntity<>(customer.getAvatar(), headers, HttpStatus.OK);
        } else {
            // Trả về ảnh mặc định
            try (InputStream is = getClass().getResourceAsStream("/static/img/default-avatar.png")) {
                if (is != null) {
                    byte[] defaultAvatar = is.readAllBytes();
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.IMAGE_PNG);
                    return new ResponseEntity<>(defaultAvatar, headers, HttpStatus.OK);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ResponseEntity.notFound().build();
        }
    }
    /**
     * Xử lý quên mật khẩu
     */
    @PostMapping("/forgot-password")
    public String forgotPassword(
            @ModelAttribute("forgotPasswordRequest") @Valid ForgotPasswordRequest form,
            BindingResult bindingResult,
            Model model
    ) {
        model.addAttribute("customerProfile", new ProfileUpdateRequest());
        model.addAttribute("forgotPasswordRequest", form);
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
        model.addAttribute("customer", null);
        model.addAttribute("activeTab", "forgot");

        if (bindingResult.hasErrors()) return "customer-profile";

        Customer customer = customerService.findByEmail(form.getEmail());
        if (customer == null) {
            bindingResult.rejectValue("email", "notfound", "Email này không tồn tại!");
            return "customer-profile";
        }

        // Sinh mật khẩu mới hoặc tạo link reset
        String newPassword = UUID.randomUUID().toString().substring(0, 8);
        customer.setPassword(passwordEncoder.encode(newPassword));
        customerService.save(customer);

        // Gửi mail
        String subject = "Đặt lại mật khẩu";
        String body = "Xin chào " + customer.getFullname() + ",\n\n"
                + "Mật khẩu mới của bạn là: " + newPassword + "\n\n"
                + "Vui lòng đăng nhập và đổi lại mật khẩu ngay.";
        emailService.sendEmail(customer.getEmail(), subject, body);

        model.addAttribute("message", "Hướng dẫn đặt lại mật khẩu đã được gửi!");
        return "customer-profile";
    }


    /**
     * Đổi mật khẩu
     */
    @PostMapping("/change-password")
    public String changePassword(
            @ModelAttribute("changePasswordRequest") @Valid ChangePasswordRequest form,
            BindingResult bindingResult,
            HttpSession session,
            Model model
    ) {
        String email = (String) session.getAttribute("email");
        model.addAttribute("customerProfile", new ProfileUpdateRequest());
        model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
        model.addAttribute("changePasswordRequest", form);
        model.addAttribute("activeTab", "changePassword");
        Customer customer = (email != null) ? customerService.findByEmail(email) : null;
        model.addAttribute("customer", customer);

        if (email == null) {
            model.addAttribute("error", "Bạn chưa đăng nhập.");
            return "customer-profile";
        }
        if (bindingResult.hasErrors()) return "customer-profile";
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "match", "Xác nhận mật khẩu không khớp!");
            return "customer-profile";
        }
        if (customer == null) {
            model.addAttribute("error", "Không tìm thấy thông tin khách hàng.");
            return "customer-profile";
        }

        // So sánh mã hóa
        if (!passwordEncoder.matches(form.getOldPassword(), customer.getPassword())) {
            bindingResult.rejectValue("oldPassword", "invalid", "Mật khẩu cũ không đúng!");
            return "customer-profile";
        }

        customer.setPassword(passwordEncoder.encode(form.getNewPassword()));
        customerService.save(customer);

        notificationService.createNotification("Bạn vừa đổi mật khẩu thành công.", customer);
        activityLogService.logActivity(
                "Đổi mật khẩu",
                "Khách hàng " + customer.getFullname() + " đã đổi mật khẩu thành công.",
                customer,
                null, null, null, null, null
        );


        model.addAttribute("success", "Đổi mật khẩu thành công!");
        return "customer-profile";
    }
}
