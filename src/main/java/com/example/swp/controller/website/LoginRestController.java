package com.example.swp.controller.website;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.swp.annotation.LogActivity;
import com.example.swp.dto.LoginRequest;
import com.example.swp.entity.Customer;
import com.example.swp.entity.Manager;
import com.example.swp.entity.Staff;
import com.example.swp.service.CustomerService;
import com.example.swp.service.EmailService;
import com.example.swp.service.ManagerService;
import com.example.swp.service.StaffService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/api")
public class LoginRestController {

    @Autowired
    private HttpSession session;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ManagerService managerService;

    @Autowired
    protected StaffService staffService;

    @GetMapping({"/login", "/api/login"})
    public String returnLoginPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/home-page";
        }
        model.addAttribute("sessionId", session.getId());
        return "login";

}

    @LogActivity(action = "Người dùng đăng nhập vào hệ thống")
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Tài khoản hoặc mật khẩu không chính xác.");
            }

            // Ghi nhận vào Spring Security Context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Thiết lập session
            session.setMaxInactiveInterval(6000); // 10 phút
            session.setAttribute("email", loginRequest.getEmail());

            // Lấy role của người dùng
            String role = authentication.getAuthorities().iterator().next().getAuthority();
            String redirectUrl = "/home-page"; // mặc định

            switch (role) {
                case "MANAGER":
                    Manager manager = managerService.findByEmail(loginRequest.getEmail());
                    if (manager != null) {
                        session.setAttribute("loggedInManager", manager);
                    }
                    redirectUrl = "/admin/manager-dashboard";
                    break;
                case "CUSTOMER":
                    Customer customer = customerService.findByEmail(loginRequest.getEmail());
                    if (customer != null) {
                        session.setAttribute("loggedInCustomer", customer);
                    }
                    redirectUrl = "/home-page";
                    break;
                case "STAFF":
                    String email = loginRequest.getEmail();
                    Staff staff = staffService.findByEmail(email).orElse(null);
                        if(staff != null) {
                        session.setAttribute("loggedInStaff", staff);
                        }
                    redirectUrl = "/admin/manager-dashboard";
                    break;
                default:
                    break;
            }

            // Lưu context vào session
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            // Trả về URL để redirect
            Map<String, String> response = new HashMap<>();
            response.put("redirect", redirectUrl);
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Email hoặc mật khẩu không chính xác.");
        } catch (Exception e) {
            e.printStackTrace(); // debug
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi đăng nhập.");
        }
    }


    @LogActivity(action = "Người dùng đăng xuất khỏi hệ thống")
    @GetMapping("/logout")
    public String logout() {
        SecurityContextHolder.clearContext();
        session.invalidate();
        return "redirect:/api/login"; // Chuyển về trang login
    }


    @GetMapping("/check-session")
    @ResponseBody
    public ResponseEntity<String> checkSession() {
        Object email = session.getAttribute("email");
        if (email != null) {
            return ResponseEntity.ok("Đang đăng nhập với email: " + email);
        } else {
            return ResponseEntity.ok("Chưa đăng nhập hoặc session đã hết hạn.");
        }
    }
}
