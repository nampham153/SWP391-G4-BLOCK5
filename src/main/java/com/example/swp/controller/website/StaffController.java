package com.example.swp.controller.website;

import com.example.swp.dto.StaffRequest;
import com.example.swp.entity.Staff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {
    @Autowired
    private StaffService staffService;
    @GetMapping("/allstaff")
    public List<Staff> getAllStaff() {
        return staffService.getAllStaff();
    }

    @PostMapping("/createstaff")
    public ResponseEntity<Staff>  createStaff(@RequestBody StaffRequest staffRequest) {
        Staff staff = staffService.createStaff(staffRequest);
        return ResponseEntity.ok(staff);
    }

}
