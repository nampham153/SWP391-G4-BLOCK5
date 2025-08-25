// src/main/java/com/example/swp/service/impl/StaffServiceImpl.java
package com.example.swp.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.swp.dto.NewStaffForm;
import com.example.swp.dto.StaffRequest;
import com.example.swp.entity.Staff;
import com.example.swp.enums.RoleName;
import com.example.swp.repository.CustomerRepository;
import com.example.swp.repository.StaffRepository;
import com.example.swp.service.EmailService;
import com.example.swp.service.StaffService;

@Service
public class StaffServiceImpl implements StaffService {

    @Autowired private StaffRepository staffRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;
    @Autowired private CustomerRepository customerRepository;

    @Override
    public List<Staff> getAllStaff() {
        return staffRepository.findAll(Sort.by(Sort.Direction.DESC, "staffid"));
    }

    @Override
    public Staff getStaff(int id) {
        return staffRepository.findById(id).orElse(null);
    }
    @Override
    public Page<Staff> getStaffsByPage(int page, int size, Sort sort) {
        return staffRepository.findAll(PageRequest.of(page, size, sort));
    }


    @Override
    public Staff createStaff(StaffRequest staffRequest) {
        // If you still use this path somewhere, keep it simple or route to createFromForm
        NewStaffForm form = NewStaffForm.builder()
                .fullname(staffRequest.getFullname())
                .email(staffRequest.getEmail())
                .phone(staffRequest.getPhone())
                .sex(staffRequest.isSex())
                .roleName(staffRequest.getRoleName())
                .build();
        return createFromForm(form);
    }

    private boolean emailExistsAnywhere(String email) {
        return staffRepository.existsByEmailIgnoreCase(email)
                || customerRepository.existsByEmail(email);
    }

    private boolean phoneExistsAnywhere(String phone) {
        return staffRepository.existsByPhone(phone)
                || customerRepository.existsByPhone(phone);
    }

    @Override
    public Staff createFromForm(NewStaffForm form) {
        if (emailExistsAnywhere(form.getEmail())) {
            throw new IllegalArgumentException("duplicate-email");
        }
        if (phoneExistsAnywhere(form.getPhone())) {
            throw new IllegalArgumentException("duplicate-phone");
        }

        String rawPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 10);

        Staff s = new Staff();
        s.setFullname(form.getFullname());
        s.setEmail(form.getEmail());
        s.setPhone(form.getPhone());
        s.setSex(form.getSex());
        s.setRoleName(Objects.requireNonNullElse(form.getRoleName(), RoleName.STAFF));
        s.setPassword(passwordEncoder.encode(rawPassword));

        try {
            Staff saved = staffRepository.save(s);
            emailService.sendEmail(saved.getEmail(),
                    "Tài khoản nhân viên tại QVL Storage",
                    "Xin chào " + saved.getFullname() + ",\n\n"
                            + "Tài khoản nhân viên của bạn đã được tạo.\n"
                            + "Email đăng nhập: " + saved.getEmail() + "\n"
                            + "Mật khẩu tạm thời: " + rawPassword + "\n\n"
                            + "Vui lòng đăng nhập và đổi mật khẩu ngay sau khi vào hệ thống."
            );
            return saved;
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("duplicate-email-or-phone", ex);
        }
    }

    @Override
    public Page<Staff> getStaffsByPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "staffid"));
        return staffRepository.findAll(pageable);
    }

    @Override
    public int countAllStaff() {
        return (int) staffRepository.count();
    }

    @Override
    public Optional<Staff> findByEmail(String email) {
        return staffRepository.findByEmail(email);
    }

    @Override
    public Optional<Staff> findById(int id) {
        return staffRepository.findById(id);
    }

    @Override
    public Staff save(Staff staff) {
        return staffRepository.save(staff);
    }

    @Override
    public void deleteById(int id) {
        try {
            staffRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            // FK constraint from orders/attendance/etc.
            throw new IllegalStateException("Không thể xoá vì nhân viên đang được liên kết với dữ liệu khác.", ex);
        }
    }

    @Override
    public boolean emailExists(String email) {
        return staffRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean phoneExists(String phone) {
        return staffRepository.existsByPhone(phone);
    }
}
