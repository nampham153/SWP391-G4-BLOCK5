package com.example.swp.service;

import com.example.swp.dto.StaffRequest;
import com.example.swp.entity.Staff;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface StaffService {

     List<Staff> getAllStaff();
     public Staff getStaff(int id);

     Staff createStaff(StaffRequest staffRequest);

     Page<Staff> getStaffsByPage(int page, int size);
     int countAllStaff();

     Optional<Staff> findByEmail(String email);
     Optional<Staff> findById(int id);
     Staff save(Staff staff);
}
