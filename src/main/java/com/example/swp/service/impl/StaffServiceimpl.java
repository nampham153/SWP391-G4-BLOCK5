package com.example.swp.service.impl;

import com.example.swp.dto.StaffRequest;
import com.example.swp.entity.Staff;
import com.example.swp.repository.StaffRepository;
import com.example.swp.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class StaffServiceimpl implements StaffService {
@Autowired
private StaffRepository staffRepository;
    @Override
    public List<Staff> getAllStaff() {
        return staffRepository.findAll() ;
    }

    @Override
    public Staff createStaff(StaffRequest staffRequest) {
        Staff staff = new Staff();
        staff.setFullname(staffRequest.getFullname());
        staff.setEmail(staffRequest.getEmail());
        staff.setPassword(staffRequest.getPassword());
        staff.setPhone(staffRequest.getPhone());
        staff.setRoleName(staffRequest.getRoleName());
        staff.setSex(staffRequest.isSex());
        staff.setIdCitizenCard(staffRequest.getIdCitizenCard());
        return staffRepository.save(staff);
    }

    @Override
    public Staff getStaff(int id) {
        return null;
    }

    @Override
    public Page<Staff> getStaffsByPage(int page, int size) {
        return staffRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public int countAllStaff() {
        return staffRepository.countAllStaff();
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
}
