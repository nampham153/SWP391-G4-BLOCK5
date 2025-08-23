// src/main/java/com/example/swp/service/StaffService.java
package com.example.swp.service;

import com.example.swp.dto.NewStaffForm;
import com.example.swp.dto.StaffRequest;
import com.example.swp.entity.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface StaffService {

     List<Staff> getAllStaff();
     Staff getStaff(int id);

     Staff createStaff(StaffRequest staffRequest); // (keep old if used elsewhere)

     // NEW: create from form with mail & validation
     Staff createFromForm(NewStaffForm form);

     Page<Staff> getStaffsByPage(int page, int size);
     int countAllStaff();

     Optional<Staff> findByEmail(String email);
     Optional<Staff> findById(int id);
     Staff save(Staff staff);
     Page<Staff> getStaffsByPage(int page, int size, Sort sort);
     // NEW: delete with constraint handling
     void deleteById(int id);

     // Optional helpers
     boolean emailExists(String email);
     boolean phoneExists(String phone);
}
